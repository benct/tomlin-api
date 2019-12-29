package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.media.MediaController.Companion.parseSort
import no.tomlin.api.media.dao.MovieDao
import no.tomlin.api.media.entity.Movie.Companion.parseMovie
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media/movie")
class MovieController {

    @Autowired
    private lateinit var tmdbService: TmdbService

    @Autowired
    private lateinit var movieDao: MovieDao

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(@RequestParam query: String?, @RequestParam sort: String?, @RequestParam page: Int?) =
        movieDao.get(query, parseSort(sort), page ?: 1)

    @Secured(USER, ADMIN)
    @GetMapping("/{id}")
    fun get(@PathVariable id: String) = movieDao.get(id)

    @Secured(ADMIN)
    @PostMapping("/{id}")
    fun store(@PathVariable id: String): Boolean =
        tmdbService.fetchMedia("movie/$id")
            ?.parseMovie()
            ?.let {
                tmdbService.storePoster(it.posterPath)
                movieDao.store(it)
            } ?: false

    @Secured(ADMIN)
    @DeleteMapping
    fun delete(@RequestParam id: String) = movieDao.delete(id)

    @Secured(ADMIN)
    @PostMapping("/update")
    fun batchUpdate(@RequestParam count: Int = UPDATE_COUNT) = null

    @Secured(ADMIN)
    @PostMapping("/favourite")
    fun favourite(@RequestParam id: String, @RequestParam set: Boolean) = movieDao.favourite(id, set)

    @Secured(ADMIN)
    @PostMapping("/seen")
    fun seen(@RequestParam id: String, @RequestParam set: Boolean) = movieDao.seen(id, set)

    @Secured(USER, ADMIN)
    @GetMapping("/external", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun external(@RequestParam id: String) = tmdbService.fetchMedia("movie/$id/external_ids")

    @Secured(USER, ADMIN)
    @GetMapping("/popular", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun popular(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/popular", page)

    @Secured(USER, ADMIN)
    @GetMapping("/top", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun top(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/top_rated", page)

    @Secured(USER, ADMIN)
    @GetMapping("/now", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun now(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/now_playing", page)

    @Secured(USER, ADMIN)
    @GetMapping("/upcoming", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun upcoming(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/upcoming", page)

    @Secured(USER, ADMIN)
    @GetMapping("/similar", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun similar(@RequestParam id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("movie/$id/similar", page)

    @Secured(USER, ADMIN)
    @GetMapping("/recommended", produces = [APPLICATION_JSON_UTF8_VALUE])
    fun recommended(@RequestParam id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("movie/$id/recommendations", page)

    companion object {
        const val UPDATE_COUNT = 10
    }
}
