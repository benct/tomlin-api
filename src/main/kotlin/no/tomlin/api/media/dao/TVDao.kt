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
}
