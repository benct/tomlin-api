package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.MEDIA
import no.tomlin.api.common.Extensions.formatDuration
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.logging.LogDao
import no.tomlin.api.media.MediaController.Companion.parseSort
import no.tomlin.api.media.dao.MovieDao
import no.tomlin.api.media.entity.Movie.Companion.parseMovie
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media/movie")
class MovieController(
    private val tmdbService: TmdbService,
    private val movieDao: MovieDao,
    private val logger: LogDao
) {

    @Secured(ADMIN, MEDIA)
    @GetMapping
    fun get(
        @RequestParam query: String?,
        @RequestParam sort: String?,
        @RequestParam page: Int?
    ): PaginationResponse<Map<String, Any?>> = movieDao.get(query, parseSort(sort), page ?: 1)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/{id}")
    fun get(@PathVariable id: String): Map<String, Any?> = movieDao.get(id)

    @Secured(ADMIN, MEDIA)
    @PostMapping("/{id}")
    fun store(@PathVariable id: String, log: Boolean = true): Boolean =
        tmdbService.fetchMedia("movie/$id", mapOf("append_to_response" to "external_ids"))
            .parseMovie()
            .let { movie ->
                tmdbService.storePoster(movie.posterPath)

                movieDao.store(movie).also {
                    if (log) logger.info("Movie", "Saved/updated ${movie.id} (${movie.title})")
                }
            }

    @Secured(ADMIN, MEDIA)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Boolean =
        movieDao.getTitle(id).let { title ->
            movieDao.delete(id).also {
                if (it) logger.info("Movie", "Removed $id ($title)")
            }
        }

    @Secured(ADMIN, MEDIA)
    @PostMapping("/update/{count}")
    fun batchUpdate(@PathVariable count: Int?): Int =
        movieDao.getIds(count ?: UPDATE_COUNT).count { store(it.toString(), false) }

    @Secured(ADMIN, MEDIA)
    @PostMapping("/update/all")
    fun batchUpdateAll(): Int {
        val start = System.currentTimeMillis()
        val count = movieDao.getIds().count {
            try {
                store(it.toString(), false)
            } catch (e: Exception) {
                logger.error(e)
                false
            }
        }
        val elapsed = System.currentTimeMillis() - start
        logger.info("Movie", "Updated $count movies in ${elapsed.formatDuration()}")
        return count
    }

    @Secured(ADMIN, MEDIA)
    @PostMapping("/favourite/{id}")
    fun favourite(@PathVariable id: String, @RequestParam set: Boolean): Boolean = movieDao.favourite(id, set)

    @Secured(ADMIN, MEDIA)
    @PostMapping("/seen/{id}")
    fun seen(@PathVariable id: String, @RequestParam set: Boolean): Boolean = movieDao.seen(id, set)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/external/{id}", produces = [APPLICATION_JSON_VALUE])
    fun external(@PathVariable id: String) = tmdbService.fetchMedia("movie/$id/external_ids")

    @Secured(ADMIN, MEDIA)
    @GetMapping("/popular", produces = [APPLICATION_JSON_VALUE])
    fun popular(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/popular", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/top", produces = [APPLICATION_JSON_VALUE])
    fun top(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/top_rated", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/now", produces = [APPLICATION_JSON_VALUE])
    fun now(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/now_playing", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/upcoming", produces = [APPLICATION_JSON_VALUE])
    fun upcoming(@RequestParam page: Int?) = tmdbService.fetchMedia("movie/upcoming", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/similar/{id}", produces = [APPLICATION_JSON_VALUE])
    fun similar(@PathVariable id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("movie/$id/similar", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/recommended/{id}", produces = [APPLICATION_JSON_VALUE])
    fun recommended(@PathVariable id: String, @RequestParam page: Int?) =
        tmdbService.fetchMedia("movie/$id/recommendations", page)

    companion object {
        const val UPDATE_COUNT = 10
    }
}
