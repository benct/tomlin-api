package no.tomlin.api.media.dao

import no.tomlin.api.common.Constants.PAGE_SIZE
import no.tomlin.api.common.Constants.TABLE_MOVIE
import no.tomlin.api.media.MediaController.MediaResponse
import no.tomlin.api.media.entity.Movie
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class MovieDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun get(id: String): Map<String, Any?> =
        jdbcTemplate.queryForMap("SELECT *, 'movie' AS `type` FROM $TABLE_MOVIE WHERE `id` = :id", mapOf("id" to id))

    fun get(query: String?, sort: String, page: Int): MediaResponse {
        val where = query?.let { "WHERE title LIKE :query" }.orEmpty()
        val start = (page - 1) * PAGE_SIZE

        val movies = jdbcTemplate.queryForList(
            "SELECT * FROM $TABLE_MOVIE $where ORDER BY $sort LIMIT $PAGE_SIZE OFFSET $start",
            mapOf("query" to "%$query%"))

        val total = jdbcTemplate.queryForObject(
            "SELECT COUNT(id) total FROM $TABLE_MOVIE $where",
            mapOf("query" to "%$query%"), Int::class.java) ?: 1

        return MediaResponse(page, total, movies)
    }

    fun getIds(count: Int? = null): List<String> = jdbcTemplate.queryForList(
        "SELECT id FROM $TABLE_MOVIE" + count?.let { " ORDER BY `updated` LIMIT $it" }.orEmpty(),
        EmptySqlParameterSource.INSTANCE,
        String::class.java)

    fun watchlist(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        "SELECT *, 'movie' AS `type` FROM $TABLE_MOVIE WHERE `seen` = false ORDER BY release_date ASC",
        EmptySqlParameterSource.INSTANCE)

    fun store(movie: Movie): Int =
        jdbcTemplate.update(movie.insertStatement(), movie.toDaoMap())

    fun delete(id: String): Int =
        jdbcTemplate.update("DELETE FROM $TABLE_MOVIE WHERE `id` = :id", mapOf("id" to id))

    fun favourite(id: String, set: Boolean): Int =
        jdbcTemplate.update("UPDATE $TABLE_MOVIE SET `favourite` = :set WHERE `id` = :id", mapOf("id" to id, "set" to set))

    fun seen(id: String, set: Boolean): Int =
        jdbcTemplate.update("UPDATE $TABLE_MOVIE SET `seen` = :set WHERE `id` = :id", mapOf("id" to id, "set" to set))

    fun stats(): Map<String, Any?> =
        mapOf(
            "years" to jdbcTemplate.queryForList(
                "SELECT SUBSTRING(release_year, 1, 3) year, COUNT(id) count FROM $TABLE_MOVIE GROUP BY year",
                EmptySqlParameterSource.INSTANCE
            ),
            "ratings" to jdbcTemplate.queryForList(
                "SELECT FLOOR(rating) score, COUNT(id) count FROM $TABLE_MOVIE GROUP BY score",
                EmptySqlParameterSource.INSTANCE
            )
        ).plus(
            jdbcTemplate.queryForMap(
                "SELECT COUNT(id) total, SUM(seen) seen, SUM(favourite) favourite, AVG(rating) rating, AVG(runtime) runtime FROM $TABLE_MOVIE",
                EmptySqlParameterSource.INSTANCE
            )
        )
}
