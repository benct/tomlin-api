package no.tomlin.api.media

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media/tv")
class TVController {

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
    @PostMapping("/external")
    fun external(@RequestParam id: String) = null

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
    @PostMapping("/popular")
    fun popular(@RequestParam page: Int = 1) = null

    @Secured(USER, ADMIN)
    @PostMapping("/top")
    fun top(@RequestParam page: Int = 1) = null

    @Secured(USER, ADMIN)
    @PostMapping("/now")
    fun now(@RequestParam page: Int = 1) = null

    @Secured(USER, ADMIN)
    @PostMapping("/upcoming")
    fun upcoming(@RequestParam page: Int = 1) = null

    @Secured(USER, ADMIN)
    @PostMapping("/similar")
    fun similar(@RequestParam id: String, @RequestParam page: Int = 1) = null

    @Secured(USER, ADMIN)
    @PostMapping("/recommended")
    fun recommended(@RequestParam id: String, @RequestParam page: Int = 1) = null

    companion object {
        const val UPDATE_COUNT = 5
    }
}
