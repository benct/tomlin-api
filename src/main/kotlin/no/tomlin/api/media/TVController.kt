package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.media.MediaController.Companion.parseSort
import no.tomlin.api.media.dao.TVDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media/tv")
class TVController {

    @Autowired
    private lateinit var tmdbService: TmdbService

    @Autowired
    private lateinit var tvDao: TVDao

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(@RequestParam query: String?, @RequestParam sort: String?, @RequestParam page: Int?) =
        tvDao.get(query, parseSort(sort), page ?: 1)

    @Secured(USER, ADMIN)
    @GetMapping("/{id}")
    fun get(@PathVariable id: String) = tvDao.get(id)

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
    fun favourite(@RequestParam id: String, @RequestParam set: Boolean) = tvDao.favourite(id, set)

    @Secured(ADMIN)
    @PostMapping("/seen")
    fun seen(@RequestParam id: String, @RequestParam set: Boolean) = tvDao.seen(id, set)

    @Secured(ADMIN)
    @PostMapping("/seenSeason")
    fun seenSeason(@RequestParam id: String, @RequestParam set: Boolean) = tvDao.seenAll(id, set)

    @Secured(USER, ADMIN)
    @GetMapping("/external", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun external(@RequestParam id: String) = tmdbService.fetchMedia("tv/$id/external_ids")

    @Secured(USER, ADMIN)
    @GetMapping("/popular", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun popular(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/popular", page)

    @Secured(USER, ADMIN)
    @GetMapping("/top", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun top(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/top_rated", page)

    @Secured(USER, ADMIN)
    @GetMapping("/now", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun now(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/on_the_air", page)

    @Secured(USER, ADMIN)
    @GetMapping("/similar", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun similar(@RequestParam id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("tv/$id/similar", page)

    @Secured(USER, ADMIN)
    @GetMapping("/recommended", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun recommended(@RequestParam id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("tv/$id/recommendations", page)

    companion object {
        const val UPDATE_COUNT = 5
    }
}
