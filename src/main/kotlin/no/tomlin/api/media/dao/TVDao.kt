package no.tomlin.api.media.dao

import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForList
import no.tomlin.api.db.Extensions.queryForMap
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.*
import no.tomlin.api.media.entity.Episode
import no.tomlin.api.media.entity.Season
import no.tomlin.api.media.entity.Stats
import no.tomlin.api.media.entity.TV
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TVDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun get(id: String): Map<String, Any?> = jdbc.queryForMap(
        Select(columns = "*, 'tv' AS type", from = TABLE_TV, where = Where("id" to id))
    ).also { tv ->
        tv["seasons"] = jdbc.queryForList(
            Select(
                columns = "id, season, title, release_date, overview",
                from = TABLE_SEASON,
                where = Where("tv_id" to id),
                orderBy = OrderBy("season")
            )
        ).map { season ->
            season.apply {
                this["episodes"] = jdbc.queryForList(
                    Select(
                        columns = "id, episode, title, release_date, overview, production_code, rating, votes, seen",
                        from = TABLE_EPISODE,
                        where = Where("season_id" to season["id"]),
                        orderBy = OrderBy("episode")
                    )
                )
            }
        }
    }

    fun get(query: String?, sort: OrderBy, page: Int): PaginationResponse<Map<String, Any?>> {
        val start = (page - 1) * PAGE_SIZE
        val where = query?.let { Where("title" to "%$it%", "id" to "%$it%", separator = "OR", like = true) }

        val tv = jdbc.queryForList(
            Select(
                columns = "t.*, $seenEpisodesSubQuery",
                from = TABLE_TV,
                where = where,
                orderBy = sort,
                limit = PAGE_SIZE,
                offset = start
            )
        )

        val total = jdbc.queryForObject(
            Select(columns = "COUNT(id)", from = TABLE_TV, where = where),
            Int::class.java
        ) ?: 1

        return PaginationResponse(page, total, tv)
    }

    fun getIds(count: Int? = null): List<Long> = jdbc.queryForList(
        Select(columns = "id", from = TABLE_TV, orderBy = OrderBy("updated"), limit = count),
        Long::class.java
    )

    fun watchlist(): List<Map<String, Any?>> = jdbc.queryForList(
        Select(
            columns = "t.*, 'tv' AS type, $seenEpisodesSubQuery",
            from = TABLE_TV,
            where = Where("seen" to false),
            orderBy = OrderBy("release_date")
        )
    )

    @CacheEvict("tvStats", allEntries = true)
    fun store(tv: TV): Boolean = jdbc.update(
        Upsert(TABLE_TV, tv.toDaoMap())
    )

    fun store(season: Season, tvId: Long): Boolean = jdbc.update(
        Upsert(TABLE_SEASON, season.toDaoMap(tvId))
    )

    fun store(episode: Episode, seasonId: Long): Boolean = jdbc.update(
        Upsert(TABLE_EPISODE, episode.toDaoMap(seasonId))
    )

    @CacheEvict("tvStats", allEntries = true)
    fun delete(id: String): Boolean {
        jdbc.update(Delete(TABLE_EPISODE, Where("tv_id" to id)))
        jdbc.update(Delete(TABLE_SEASON, Where("tv_id" to id)))
        return jdbc.update(Delete(TABLE_TV, Where("id" to id)))
    }

    fun favourite(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_TV, mapOf("favourite" to set), Where("id" to id))
    )

    fun seen(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_TV, mapOf("seen" to set), Where("id" to id))
    )

    fun seenEpisode(episodeId: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_EPISODE, mapOf("seen" to set), Where("id" to episodeId))
    )

    fun seenSeason(seasonId: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_EPISODE, mapOf("seen" to set), Where("season_id" to seasonId))
    )

    @Cacheable("tvStats")
    fun stats(): Stats? = jdbc.queryForObject(
        Select(
            columns = "COUNT(id) AS total, SUM(seen) AS seen, SUM(favourite) AS favourite, AVG(rating) AS rating, " +
                "(SELECT COUNT(id) FROM $TABLE_SEASON) AS seasons, " +
                "(SELECT COUNT(id) FROM $TABLE_EPISODE) AS episodes, " +
                "(SELECT SUM(seen) FROM $TABLE_EPISODE) AS seen_episodes ",
            from = TABLE_TV,
        )
    ) { rs, _ ->
        Stats(
            statsReleaseDecade(),
            statsGroupedRating(),
            rs.getInt("total"),
            rs.getInt("seen"),
            rs.getInt("favourite"),
            rs.getDouble("rating"),
            rs.getInt("episodes"),
            rs.getInt("seen_episodes"),
        )
    }

    fun statsReleaseDecade(): List<Stats.YearStat> = jdbc.query(
        Select(
            columns = "SUBSTRING(release_year, 1, 3) AS year, COUNT(id) AS count",
            from = TABLE_TV,
            groupBy = GroupBy("year")
        ),
        Stats.YearStat.rowMapper,
    )

    fun statsGroupedRating(): List<Stats.RatingStat> = jdbc.query(
        Select(columns = "FLOOR(rating) AS score, COUNT(id) AS count", from = TABLE_TV, groupBy = GroupBy("score")),
        Stats.RatingStat.rowMapper,
    )

    private companion object {
        val seenEpisodesSubQuery =
            "(SELECT COUNT(id) FROM $TABLE_EPISODE e WHERE e.seen = 1 AND e.tv_id = t.id) AS seen_episodes"
    }
}
