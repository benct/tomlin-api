package no.tomlin.api.media.dao

import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.common.Sort
import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForList
import no.tomlin.api.db.Extensions.queryForMap
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.*
import no.tomlin.api.db.Update
import no.tomlin.api.db.Upsert
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
        Select(TABLE_TV)
            .columns("*")
            .column("'tv'").custom("type")
            .where("id").eq(id)
    ).also { tv ->
        tv["seasons"] = jdbc.queryForList(
            Select(TABLE_SEASON)
                .columns("id", "season", "title", "release_date", "overview")
                .where("tv_id").eq(id)
                .orderBy("season")
        ).map { season ->
            season.apply {
                this["episodes"] = jdbc.queryForList(
                    Select(TABLE_EPISODE)
                        .columns(
                            "id", "episode", "title", "release_date", "overview",
                            "production_code", "rating", "votes", "seen"
                        )
                        .where("season_id").eq(season["id"])
                        .orderBy("episode")
                )
            }
        }
    }

    fun get(query: String?, sort: Sort, page: Int): PaginationResponse<Map<String, Any?>> {
        val start = (page - 1) * PAGE_SIZE

        fun Select.optionalWhere(): Select =
            query?.let { this.where("title").like("%$query%").or("id").like("%$query%") } ?: this

        val tv = jdbc.queryForList(
            Select(TABLE_TV)
                .columns("*")
                .column(seenEpisodesSubQuery, "seen_episodes")
                .optionalWhere()
                .orderBy(*sort.toPairs())
                .limit(PAGE_SIZE, offset = start)
        )

        val total = jdbc.queryForObject(
            Select(TABLE_TV)
                .column("id").count()
                .optionalWhere(),
            Int::class.java
        ) ?: 1

        return PaginationResponse(page, total, tv)
    }

    fun getIds(count: Int? = null): List<Long> = jdbc.queryForList(
        Select(TABLE_TV).columns("id").orderBy("updated").limit(count),
        Long::class.java
    )

    fun watchlist(): List<Map<String, Any?>> = jdbc.queryForList(
        Select(TABLE_TV)
            .columns("*")
            .column("'tv'").custom("type")
            .column(seenEpisodesSubQuery, "seen_episodes")
            .where("seen").eq(false)
            .orderBy("release_date")
    )

    @CacheEvict("tvStats", allEntries = true)
    fun store(tv: TV): Boolean = jdbc.update(
        Upsert(TABLE_TV).data(tv.toDaoMap())
    )

    fun store(season: Season, tvId: Long): Boolean = jdbc.update(
        Upsert(TABLE_SEASON).data(season.toDaoMap(tvId))
    )

    fun store(episode: Episode, seasonId: Long): Boolean = jdbc.update(
        Upsert(TABLE_EPISODE).data(episode.toDaoMap(seasonId))
    )

    @CacheEvict("tvStats", allEntries = true)
    fun delete(id: String): Boolean {
        jdbc.update(Delete(TABLE_EPISODE).where("tv_id").eq(id))
        jdbc.update(Delete(TABLE_SEASON).where("tv_id").eq(id))
        return jdbc.update(Delete(TABLE_TV).where("id").eq(id))
    }

    @CacheEvict("tvStats", allEntries = true)
    fun deleteSeason(seasonId: String): Boolean =
        jdbc.update(Delete(TABLE_SEASON).where("id").eq(seasonId))

    @CacheEvict("tvStats", allEntries = true)
    fun deleteEpisode(episodeId: String): Boolean =
        jdbc.update(Delete(TABLE_EPISODE).where("id").eq(episodeId))

    fun favourite(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_TV)
            .set("favourite" to set)
            .where("id").eq(id)
    )

    fun seen(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_TV)
            .set("seen" to set)
            .where("id").eq(id)
    )

    fun seenEpisode(episodeId: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_EPISODE)
            .set("seen" to set)
            .where("id").eq(episodeId)
    )

    fun seenSeason(seasonId: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_EPISODE)
            .set("seen" to set)
            .where("season_id").eq(seasonId)
    )

    @Cacheable("tvStats")
    fun stats(): Stats? = jdbc.queryForObject(
        Select(TABLE_TV)
            .column("id").count("total")
            .column("seen").sum("seen")
            .column("favourite").sum("favourite")
            .column("rating").avg("rating")
            .column(Select(TABLE_SEASON).column("id").count(), "seasons")
            .column(Select(TABLE_EPISODE).column("id").count(), "episodes")
            .column(Select(TABLE_EPISODE).column("seen").sum(), "seen_episodes")
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
        Select(TABLE_TV)
            .column("release_year").subString(1, 3, "year")
            .column("id").count("count")
            .groupByAlias("year"),
        Stats.YearStat.rowMapper,
    )

    fun statsGroupedRating(): List<Stats.RatingStat> = jdbc.query(
        Select(TABLE_TV)
            .column("rating").floor("score")
            .column("id").count("count")
            .groupByAlias("score"),
        Stats.RatingStat.rowMapper,
    )

    private companion object {
        val seenEpisodesSubQuery =
            Select(TABLE_EPISODE)
                .column("id").count()
                .where("seen").eq(true)
                .and("tv_id").eq(TABLE_TV, "id")
    }
}
