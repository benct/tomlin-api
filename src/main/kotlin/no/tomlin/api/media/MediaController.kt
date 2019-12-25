package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media")
class MediaController {

    @Autowired
    private lateinit var tmdbService: TmdbService

    @GetMapping
    fun stats() = null

    @Secured(USER, ADMIN)
    @GetMapping("/get")
    fun get(@RequestParam type: String, @RequestParam id: String) = null

    @Secured(USER, ADMIN)
    @GetMapping("/watchlist")
    fun watchlist() = null

    @Secured(USER, ADMIN)
    @GetMapping("/search", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun search(@RequestParam query: String) = tmdbService.fetchMedia("search/multi", query)

    @Secured(ADMIN)
    @PostMapping("/posters")
    fun posters(@RequestParam(required = false) overwrite: Boolean = false) = null

    companion object {
        fun parseSort(sort: String): String = when (sort) {
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
