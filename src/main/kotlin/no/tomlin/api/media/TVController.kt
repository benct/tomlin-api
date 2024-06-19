package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.MEDIA
import no.tomlin.api.common.Extensions.formatDuration
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.logging.LogDao
import no.tomlin.api.media.MediaController.Companion.parseSort
import no.tomlin.api.media.dao.TVDao
import no.tomlin.api.media.entity.Season.Companion.parseSeason
import no.tomlin.api.media.entity.TV.Companion.parseTV
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media/tv")
class TVController(
    private val tmdbService: TmdbService,
    private val tvDao: TVDao,
    private val logger: LogDao
) {

    @Secured(ADMIN, MEDIA)
    @GetMapping
    fun get(
        @RequestParam query: String?,
        @RequestParam sort: String?,
        @RequestParam page: Int?
    ): PaginationResponse<Map<String, Any?>> = tvDao.get(query, parseSort(sort), page ?: 1)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/{id}")
    fun get(@PathVariable id: String): Map<String, Any?> = tvDao.get(id)

    @Secured(ADMIN, MEDIA)
    @PostMapping("/{id}")
    fun store(@PathVariable id: String, log: Boolean = true): Boolean =
        tmdbService.fetchMedia("tv/$id", mapOf("append_to_response" to "external_ids"))
            .parseTV()
            .let { tv ->
                tmdbService.storePoster(tv.posterPath)

                tv.seasons.forEach { s ->
                    tmdbService.fetchMedia("tv/${id}/season/${s.seasonNumber}")
                        .parseSeason()
                        .let { season ->
                            season.episodes.forEach { episode ->
                                tvDao.store(episode, season.id)
                            }
                            tvDao.store(season, tv.id)
                        }
                }
                tvDao.store(tv).also {
                    if (log) logger.info("TV", "Saved/updated ${tv.id} (${tv.name})")
                }
            }

    @Secured(ADMIN, MEDIA)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Boolean =
        tvDao.getTitle(id).let { title ->
            tvDao.delete(id).also {
                if (it) logger.info("TV", "Removed $id ($title)")
            }
        }

    @Secured(ADMIN, MEDIA)
    @DeleteMapping("/season/{id}")
    fun deleteSeason(@PathVariable id: String): Boolean =
        tvDao.deleteSeason(id)

    @Secured(ADMIN, MEDIA)
    @DeleteMapping("/episode/{id}")
    fun deleteEpisode(@PathVariable id: String): Boolean =
        tvDao.deleteEpisode(id)

    @Secured(ADMIN, MEDIA)
    @PostMapping("/update/{count}")
    fun batchUpdate(@PathVariable count: Int?): Int =
        tvDao.getIds(count ?: UPDATE_COUNT).count { store(it.toString(), false) }

    @Secured(ADMIN, MEDIA)
    @PostMapping("/update/all")
    fun batchUpdateAll(): Int {
        val start = System.currentTimeMillis()
        val count = tvDao.getIds().count {
            try {
                store(it.toString(), false)
            } catch (e: Exception) {
                logger.error(e)
                false
            }
        }
        val elapsed = System.currentTimeMillis() - start
        logger.info("Movie", "Updated $count TV shows in ${elapsed.formatDuration()}")
        return count
    }

    @Secured(ADMIN, MEDIA)
    @PostMapping("/favourite/{id}")
    fun favourite(@PathVariable id: String, @RequestParam set: Boolean): Boolean = tvDao.favourite(id, set)

    @Secured(ADMIN, MEDIA)
    @PostMapping("/seen/{id}")
    fun seen(@PathVariable id: String, @RequestParam set: Boolean): Boolean = tvDao.seen(id, set)

    @Secured(ADMIN, MEDIA)
    @PostMapping("/seen/episode/{id}")
    fun seenEpisode(@PathVariable id: String, @RequestParam set: Boolean): Boolean = tvDao.seenEpisode(id, set)

    @Secured(ADMIN, MEDIA)
    @PostMapping("/seen/season/{id}")
    fun seenSeason(@PathVariable id: String, @RequestParam set: Boolean): Boolean = tvDao.seenSeason(id, set)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/external/{id}", produces = [APPLICATION_JSON_VALUE])
    fun external(@PathVariable id: String) = tmdbService.fetchMedia("tv/$id/external_ids")

    @Secured(ADMIN, MEDIA)
    @GetMapping("/popular", produces = [APPLICATION_JSON_VALUE])
    fun popular(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/popular", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/top", produces = [APPLICATION_JSON_VALUE])
    fun top(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/top_rated", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/now", produces = [APPLICATION_JSON_VALUE])
    fun now(@RequestParam page: Int?) = tmdbService.fetchMedia("tv/on_the_air", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/similar/{id}", produces = [APPLICATION_JSON_VALUE])
    fun similar(@PathVariable id: String, @RequestParam page: Int?) = tmdbService.fetchMedia("tv/$id/similar", page)

    @Secured(ADMIN, MEDIA)
    @GetMapping("/recommended/{id}", produces = [APPLICATION_JSON_VALUE])
    fun recommended(@PathVariable id: String, @RequestParam page: Int?) =
        tmdbService.fetchMedia("tv/$id/recommendations", page)

    companion object {
        const val UPDATE_COUNT = 3
    }
}
