package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.media.dao.MovieDao
import no.tomlin.api.media.dao.TVDao
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/media")
class MediaController(
    private val tmdbService: TmdbService,
    private val movieDao: MovieDao,
    private val tvDao: TVDao
) {

    @GetMapping
    fun stats() = mapOf(
        "movie" to movieDao.stats(),
        "tv" to tvDao.stats()
    )

    @Secured(USER, ADMIN)
    @GetMapping("/watchlist")
    fun watchlist() = PaginationResponse(movieDao.watchlist() + tvDao.watchlist())

    @Secured(USER, ADMIN)
    @GetMapping("/search", produces = [APPLICATION_JSON_VALUE])
    fun search(@RequestParam query: String): String = tmdbService.fetchMedia("search/multi", query)

    @Secured(USER, ADMIN)
    @GetMapping("/existing")
    fun existing() = mapOf(
        "movie" to movieDao.getIds(),
        "tv" to tvDao.getIds()
    )

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
}
