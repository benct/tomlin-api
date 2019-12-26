package no.tomlin.api.media.dao

import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.Constants.TABLE_EPISODE
import no.tomlin.api.common.Constants.TABLE_SEASON
import no.tomlin.api.common.Constants.TABLE_TV
import no.tomlin.api.media.entity.MediaResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class TVDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun get(id: String): Map<String, Any?> = jdbcTemplate.queryForMap("SELECT * FROM $TABLE_TV WHERE `id` = :id", mapOf("id" to id))

    fun get(query: String?, sort: String, page: Int): MediaResponse {
        val where = query?.let { "WHERE title LIKE :query" }.orEmpty()
        val start = (page - 1) * PAGE_SIZE

        val tv = jdbcTemplate.queryForList(
            "SELECT t.*, (SELECT COUNT(id) FROM $TABLE_EPISODE e WHERE e.seen = 1 AND e.tv_id = t.id) AS seen_episodes " +
                "FROM $TABLE_TV t $where ORDER BY $sort LIMIT $PAGE_SIZE OFFSET $start",
            mapOf("query" to "%$query%"))

        val total = jdbcTemplate.queryForObject(
            "SELECT COUNT(id) total FROM $TABLE_TV $where",
            mapOf("query" to "%$query%"), Int::class.java) ?: 1

        return MediaResponse(page, total, tv)
    }

    fun watchlist(): List<Map<String, Any>> = jdbcTemplate.queryForList(
        "SELECT t.*, 'tv' AS `type`, " +
            "(SELECT COUNT(id) FROM $TABLE_EPISODE e WHERE e.seen = 1 AND e.tv_id = t.id) AS seen_episodes " +
            "FROM $TABLE_TV t WHERE t.seen = 0 ORDER BY t.release_date ASC",
        EmptySqlParameterSource.INSTANCE)

    fun stats(): Map<String, Any?> =
        mapOf(
            "years" to jdbcTemplate.queryForList(
                "SELECT SUBSTRING(release_year, 1, 3) year, COUNT(id) count FROM $TABLE_TV GROUP BY year",
                EmptySqlParameterSource.INSTANCE
            ),
            "ratings" to jdbcTemplate.queryForList(
                "SELECT FLOOR(rating) score, COUNT(id) count FROM $TABLE_TV GROUP BY score",
                EmptySqlParameterSource.INSTANCE
            )
        ).plus(
            jdbcTemplate.queryForMap(
                "SELECT COUNT(id) total, SUM(seen) seen, SUM(favourite) favourite, AVG(rating) rating, " +
                    "(SELECT COUNT(id) FROM $TABLE_SEASON) seasons FROM $TABLE_TV",
                EmptySqlParameterSource.INSTANCE
            )
        ).plus(
            jdbcTemplate.queryForMap(
                "SELECT COUNT(id) episodes, SUM(seen) seen_episodes FROM $TABLE_EPISODE",
                EmptySqlParameterSource.INSTANCE
            )
        )

    fun favourite(id: String, set: Boolean): Boolean =
        jdbcTemplate.update("UPDATE $TABLE_TV SET `favourite` = :set WHERE `id` = :id", mapOf("id" to id, "set" to set)) > 0

    fun seen(id: String, set: Boolean): Boolean =
        jdbcTemplate.update("UPDATE $TABLE_TV SET `seen` = :set WHERE `id` = :id", mapOf("id" to id, "set" to set)) > 0

    fun seenAll(seasonId: String, set: Boolean): Boolean =
        jdbcTemplate.update("UPDATE $TABLE_EPISODE SET `seen` = :set WHERE `season_id` = :id", mapOf("id" to seasonId, "set" to set)) > 0

    fun delete(id: String): Boolean {
        jdbcTemplate.update("DELETE FROM $TABLE_EPISODE WHERE `tv_id` = :id", mapOf("id" to id))
        jdbcTemplate.update("DELETE FROM $TABLE_SEASON WHERE `tv_id` = :id", mapOf("id" to id))
        return jdbcTemplate.update("DELETE FROM $TABLE_TV WHERE `id` = :id", mapOf("id" to id)) > 0
    }
}
