package no.tomlin.api.media.dao

import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForList
import no.tomlin.api.db.Extensions.queryForMap
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.TABLE_MOVIE
import no.tomlin.api.media.entity.Movie
import no.tomlin.api.media.entity.Stats
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class MovieDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun get(id: String): Map<String, Any?> = jdbc.queryForMap(
        Select(columns = "*, 'movie' AS type", from = TABLE_MOVIE, where = Where("id" to id))
    )

    fun get(query: String?, sort: OrderBy, page: Int): PaginationResponse<Map<String, Any?>> {
        val start = (page - 1) * PAGE_SIZE
        val where = query?.let { Where("title" to "%$it%", "id" to "%$it%", separator = "OR", like = true) }

        val movies = jdbc.queryForList(
            Select(
                from = TABLE_MOVIE,
                where = where,
                orderBy = sort,
                limit = PAGE_SIZE,
                offset = start,
            ),
        )

        val total = jdbc.queryForObject(
            Select(columns = "COUNT(id)", from = TABLE_MOVIE, where = where),
            Int::class.java
        ) ?: 1

        return PaginationResponse(page, total, movies)
    }

    fun getIds(count: Int? = null): List<Long> = jdbc.queryForList(
        Select(columns = "id", from = TABLE_MOVIE, orderBy = OrderBy("updated"), limit = count),
        Long::class.java
    )

    fun watchlist(): List<Map<String, Any?>> = jdbc.queryForList(
        Select(
            columns = "*, 'movie' AS type",
            from = TABLE_MOVIE,
            where = Where("seen" to false),
            orderBy = OrderBy("release_date")
        )
    )

    @CacheEvict("movieStats", allEntries = true)
    fun store(movie: Movie): Boolean = jdbc.update(
        Upsert(TABLE_MOVIE, movie.toDaoMap())
    )

    @CacheEvict("movieStats", allEntries = true)
    fun delete(id: String): Boolean = jdbc.update(
        Delete(TABLE_MOVIE, Where("id" to id))
    )

    fun favourite(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_MOVIE, mapOf("favourite" to set), Where("id" to id))
    )

    fun seen(id: String, set: Boolean): Boolean = jdbc.update(
        Update(TABLE_MOVIE, mapOf("seen" to set), Where("id" to id))
    )

    @Cacheable("movieStats")
    fun stats(): Stats? = jdbc.queryForObject(
        Select(
            columns = "COUNT(id) AS total, SUM(seen) AS seen, SUM(favourite) AS favourite, AVG(rating) AS rating",
            from = TABLE_MOVIE
        ),
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
        Select(
            columns = "SUBSTRING(release_year, 1, 3) AS year, COUNT(id) AS count",
            from = TABLE_MOVIE,
            groupBy = GroupBy("year")
        ),
        Stats.YearStat.rowMapper,
    )

    fun statsGroupedRating(): List<Stats.RatingStat> = jdbc.query(
        Select(
            columns = "FLOOR(rating) AS score, COUNT(id) AS count",
            from = TABLE_MOVIE,
            groupBy = GroupBy("score"),
        ),
        Stats.RatingStat.rowMapper,
    )
}
