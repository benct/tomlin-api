package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.logging.LogDao
import no.tomlin.api.media.MediaController.Companion.parseSort
import no.tomlin.api.media.MediaController.MediaResponse
import no.tomlin.api.media.dao.TVDao
import no.tomlin.api.media.entity.Season.Companion.parseSeason
import no.tomlin.api.media.entity.TV.Companion.parseTV
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

    @Autowired
    private lateinit var logger: LogDao

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(@RequestParam query: String?, @RequestParam sort: String?, @RequestParam page: Int?): MediaResponse =
        tvDao.get(query, parseSort(sort), page ?: 1)

    @Secured(USER, ADMIN)
    @GetMapping("/{id}")
    fun get(@PathVariable id: String): Map<String, Any?> = tvDao.get(id)

    @Secured(ADMIN)
    @PostMapping("/{id}")
    fun store(@PathVariable id: String): Boolean =
        tmdbService.fetchMedia("tv/$id", mapOf("append_to_response" to "external_ids"))
            .parseTV()
            .let { tv ->
                tmdbService.storePoster(tv.posterPath)

                tv.seasons.all { s ->
                    tmdbService.fetchMedia("tv/${id}/season/${s.seasonNumber}")
                        .parseSeason()
                        .let { season ->
                            season.episodes.all { episode ->
                                tvDao.store(episode.insertStatement(false), episode.toDaoMap(season.id)) == 1
                            }.and(
                                tvDao.store(season.insertStatement(false), season.toDaoMap(tv.id)) == 1
                            )
                        }
                }.and(
                    tvDao.store(tv.insertStatement(), tv.toDaoMap()).let {
                        if (it == 1) {
                            logger.info("TV", "Saved/updated ${tv.id} (${tv.name})")
                            true
                        } else false
                    }
                )
            }

    @Secured(ADMIN)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Boolean = tvDao.delete(id).let {
        if (it == 1) {
            logger.info("TV", "Removed $id")
            true
        } else false
    }

    @Secured(ADMIN)
    @PostMapping("/update")
    fun batchUpdate(@RequestParam count: Int?): Int = tvDao.getIds(count ?: UPDATE_COUNT).count { store(it) }

    @Secured(ADMIN)
    @PostMapping("/favourite")
    fun favourite(@RequestParam id: String, @RequestParam set: Boolean): Boolean = tvDao.favourite(id, set) == 1

    @Secured(ADMIN)
    @PostMapping("/seen")
    fun seen(@RequestParam id: String, @RequestParam set: Boolean): Boolean = tvDao.seen(id, set) == 1

    @Secured(ADMIN)
    @PostMapping("/seenSeason")
    fun seenSeason(@RequestParam id: String, @RequestParam set: Boolean): Boolean = tvDao.seenAll(id, set) == 1

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
        const val UPDATE_COUNT = 3
    }
}
