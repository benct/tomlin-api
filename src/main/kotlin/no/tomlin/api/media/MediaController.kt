package no.tomlin.api.media

import no.tomlin.api.common.Constants
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.media.dao.MovieDao
import no.tomlin.api.media.dao.TVDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.math.ceil

@RestController
@RequestMapping("/media")
class MediaController {

    @Autowired
    private lateinit var tmdbService: TmdbService

    @Autowired
    private lateinit var movieDao: MovieDao

    @Autowired
    private lateinit var tvDao: TVDao

    @GetMapping
    fun stats() = mapOf(
        "movie" to movieDao.stats(),
        "tv" to tvDao.stats()
    )

    @Secured(USER, ADMIN)
    @GetMapping("/watchlist")
    fun watchlist() = movieDao.watchlist() + tvDao.watchlist()

    @Secured(USER, ADMIN)
    @GetMapping("/search", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun search(@RequestParam query: String) = tmdbService.fetchMedia("search/multi", query)

    companion object {
        fun parseSort(sort: String?): String = when (sort) {
            "rating-asc" -> "rating ASC"
            "rating-desc" -> "rating DESC"
            "release-asc" -> "release_date ASC"
            "release-desc" -> "release_date DESC"
            "title-asc" -> "title ASC"
            "title-desc" -> "title DESC"
            "favourite" -> "favourite DESC, rating DESC"
            else -> "rating DESC"
        }
    }

    data class MediaResponse(
        val page: Int,
        val total_pages: Int,
        val total_results: Int,
        val results: List<Map<String, Any?>>
    ) {
        constructor(page: Int, total: Int, data: List<Map<String, Any?>>) : this(
            page,
            ceil(total.toDouble() / Constants.PAGE_SIZE).toInt(),
            total,
            data
        )
    }
}
