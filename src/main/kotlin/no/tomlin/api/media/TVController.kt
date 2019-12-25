package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media/tv")
class TVController {

    @Autowired
    private lateinit var tmdbService: TmdbService

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(@RequestParam query: String?, @RequestParam sort: String?, @RequestParam page: Int? = 1) = null

    @Secured(ADMIN)
    @PostMapping
    fun store(@RequestParam id: String) = null

    @Secured(ADMIN)
    @DeleteMapping
    fun delete(@RequestParam id: String) = null

    @Secured(ADMIN)
    @PostMapping("/update")
    fun batchUpdate(@RequestParam count: Int = UPDATE_COUNT) = null

    @Secured(ADMIN)
    @PostMapping("/favourite")
    fun favourite(@RequestParam id: String, @RequestParam set: Boolean) = null

    @Secured(ADMIN)
    @PostMapping("/seen")
    fun seen(@RequestParam id: String, @RequestParam set: Boolean) = null

    @Secured(ADMIN)
    @PostMapping("/seenSeason")
    fun seenSeason(@RequestParam id: String, @RequestParam set: Boolean) = null

    @Secured(USER, ADMIN)
    @GetMapping("/external", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun external(@RequestParam id: String) = tmdbService.fetchMedia("tv/$id/external_ids")

    @Secured(USER, ADMIN)
    @GetMapping("/popular", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun popular(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/popular", page)

    @Secured(USER, ADMIN)
    @GetMapping("/top", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun top(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/top_rated", page)

    @Secured(USER, ADMIN)
    @GetMapping("/now", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun now(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/on_the_air", page)

    @Secured(USER, ADMIN)
    @GetMapping("/similar", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun similar(@RequestParam id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("tv/$id/similar", page)

    @Secured(USER, ADMIN)
    @GetMapping("/recommended", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun recommended(@RequestParam id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("tv/$id/recommendations", page)

    companion object {
        const val UPDATE_COUNT = 5
    }
}
