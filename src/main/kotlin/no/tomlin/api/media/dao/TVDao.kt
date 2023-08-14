package no.tomlin.api.media.dao

import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.Constants.TABLE_EPISODE
import no.tomlin.api.common.Constants.TABLE_SEASON
import no.tomlin.api.common.Constants.TABLE_TV
import no.tomlin.api.common.Extensions.checkRowsAffected
import no.tomlin.api.common.PaginationResponse
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TVDao(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun get(id: String): Map<String, Any?> =
        jdbcTemplate.queryForMap("SELECT *, 'tv' AS `type` FROM $TABLE_TV WHERE `id` = :id", mapOf("id" to id))
            .also { tv ->
                tv["seasons"] = jdbcTemplate.queryForList(
                    "SELECT id, season, title, release_date, overview " +
                        "FROM $TABLE_SEASON WHERE `tv_id` = :id ORDER BY season", mapOf("id" to id)
                )
                    .map { season ->
                        season["episodes"] = jdbcTemplate.queryForList(
                            "SELECT id, episode, title, release_date, overview, production_code, rating, votes, seen " +
                                "FROM $TABLE_EPISODE WHERE `season_id` = :id ORDER BY episode",
                            mapOf("id" to season["id"])
                        )
                        season
                    }
            }

    fun get(query: String?, sort: String, page: Int): PaginationResponse<Map<String, Any?>> {
        val where = query?.let { "WHERE title LIKE :query OR id LIKE :query" }.orEmpty()
        val start = (page - 1) * PAGE_SIZE

        val tv = jdbcTemplate.queryForList(
            "SELECT t.*, (SELECT COUNT(id) FROM $TABLE_EPISODE e WHERE e.seen = 1 AND e.tv_id = t.id) AS seen_episodes " +
                "FROM $TABLE_TV t $where ORDER BY $sort LIMIT $PAGE_SIZE OFFSET $start",
            mapOf("query" to "%$query%")
        )

        val total = jdbcTemplate.queryForObject(
            "SELECT COUNT(id) total FROM $TABLE_TV $where",
            mapOf("query" to "%$query%"),
            Int::class.java
        ) ?: 1

        return PaginationResponse(page, total, tv)
    }

    fun getIds(count: Int? = null): List<Long> = jdbcTemplate.queryForList(
        "SELECT id FROM $TABLE_TV" + count?.let { " ORDER BY `updated` LIMIT $it" }.orEmpty(),
        EmptySqlParameterSource.INSTANCE,
        Long::class.java
    )

    fun watchlist(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        "SELECT t.*, 'tv' AS `type`, " +
            "(SELECT COUNT(id) FROM $TABLE_EPISODE e WHERE e.seen = 1 AND e.tv_id = t.id) AS seen_episodes " +
            "FROM $TABLE_TV t WHERE t.seen = 0 ORDER BY t.release_date ASC",
        EmptySqlParameterSource.INSTANCE
    )

    @CacheEvict("tvStats", allEntries = true)
    fun store(statement: String, data: Map<String, Any?>): Boolean =
        jdbcTemplate.update(statement, data).checkRowsAffected()

    @CacheEvict("tvStats", allEntries = true)
    fun delete(id: String): Boolean {
        jdbcTemplate.update("DELETE FROM $TABLE_EPISODE WHERE `tv_id` = :id", mapOf("id" to id))
        jdbcTemplate.update("DELETE FROM $TABLE_SEASON WHERE `tv_id` = :id", mapOf("id" to id))
        return jdbcTemplate.update("DELETE FROM $TABLE_TV WHERE `id` = :id", mapOf("id" to id)).checkRowsAffected()
    }

    fun favourite(id: String, set: Boolean): Boolean = jdbcTemplate
        .update("UPDATE $TABLE_TV SET `favourite` = :set WHERE `id` = :id", mapOf("id" to id, "set" to set))
        .checkRowsAffected()

    fun seen(id: String, set: Boolean): Boolean = jdbcTemplate
        .update("UPDATE $TABLE_TV SET `seen` = :set WHERE `id` = :id", mapOf("id" to id, "set" to set))
        .checkRowsAffected()

    fun seenEpisode(episodeId: String, set: Boolean): Boolean = jdbcTemplate
        .update("UPDATE $TABLE_EPISODE SET `seen` = :set WHERE `id` = :id", mapOf("id" to episodeId, "set" to set))
        .checkRowsAffected()

    fun seenSeason(seasonId: String, set: Boolean): Boolean = jdbcTemplate
        .update(
            "UPDATE $TABLE_EPISODE SET `seen` = :set WHERE `season_id` = :id",
            mapOf("id" to seasonId, "set" to set)
        ) > 0

    @Cacheable("tvStats")
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
                "SELECT COUNT(id) total, SUM(seen) seen, SUM(favourite) favourite, FORMAT(AVG(rating), 2) rating, " +
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
