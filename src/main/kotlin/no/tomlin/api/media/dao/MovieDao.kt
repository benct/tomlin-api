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
import no.tomlin.api.db.Table.TABLE_MOVIE
import no.tomlin.api.db.Update
import no.tomlin.api.db.Upsert
import no.tomlin.api.media.entity.Movie
import no.tomlin.api.media.entity.Stats
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class MovieDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun get(id: String): Map<String, Any?> = jdbc.queryForMap(
        Select(TABLE_MOVIE)
            .columns("*")
            .column("'movie'").custom("type")
            .where("id").eq(id)
    )

    fun get(query: String?, sort: Sort, page: Int): PaginationResponse<Map<String, Any?>> {
        val start = (page - 1) * PAGE_SIZE

        fun Select.optionalWhere(): Select =
            query?.let { this.where("title").like("%$query%").or("id").like("%$query%") } ?: this

        val movies = jdbc.queryForList(
            Select(TABLE_MOVIE)
                .optionalWhere()
                .orderBy(*sort.toPairs())
                .limit(PAGE_SIZE, offset = start)
        )

        val total = jdbc.queryForObject(
            Select(TABLE_MOVIE)
                .column("id").count()
                .optionalWhere(),
            Int::class.java
        ) ?: 1

        return PaginationResponse(page, total, movies)
    }

    fun getIds(count: Int? = null): List<Long> = jdbc.queryForList(
        Select(TABLE_MOVIE).columns("id").orderBy("updated").limit(count),
        Long::class.java
    )

    fun getTitle(id: String): String? = jdbc.queryForObject(
        Select(TABLE_MOVIE).columns("title").where("id").eq(id),
        String::class.java
    )

    fun watchlist(): List<Map<String, Any?>> = jdbc.queryForList(
        Select(TABLE_MOVIE)
            .columns("*")
            .column("'movie'").custom("type")
            .where("seen").eq(false)
            .orderBy("release_date")
    )

    @CacheEvict("movieStats", allEntries = true)
    fun store(movie: Movie): Boolean = jdbc.update(
        Upsert(TABLE_MOVIE).data(movie.toDaoMap())
    )

    @CacheEvict("movieStats", allEntries = true)
    fun delete(id: String): Boolean = jdbc.update(
        Delete(TABLE_MOVIE).where("id").eq(id)
    )

    fun favourite(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_MOVIE).set("favourite" to set).where("id").eq(id)
    )

    fun seen(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_MOVIE).set("seen" to set).where("id").eq(id)
    )

    @Cacheable("movieStats")
    fun stats(): Stats? = jdbc.queryForObject(
        Select(TABLE_MOVIE)
            .column("id").count("total")
            .column("seen").sum("seen")
            .column("favourite").sum("favourite")
            .column("rating").avg("rating")
    ) { rs, _ ->
        Stats(
            statsReleaseDecade(),
            statsGroupedRating(),
            rs.getInt("total"),
            rs.getInt("seen"),
            rs.getInt("favourite"),
            rs.getDouble("rating"),
        )
    }

    fun statsReleaseDecade(): List<Stats.YearStat> = jdbc.query(
        Select(TABLE_MOVIE)
            .column("release_year").subString(1, 3, "year")
            .column("id").count("count")
            .groupByAlias("year"),
        Stats.YearStat.rowMapper,
    )

    fun statsGroupedRating(): List<Stats.RatingStat> = jdbc.query(
        Select(TABLE_MOVIE)
            .column("rating").floor("score")
            .column("id").count("count")
            .groupByAlias("score"),
        Stats.RatingStat.rowMapper,
    )
}
