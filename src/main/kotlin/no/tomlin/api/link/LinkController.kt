package no.tomlin.api.link

import no.tomlin.api.common.AuthUtils.hasRole
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.PRIVATE
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.link.LinkDao.Link
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.net.URL

@RestController
@RequestMapping("/link")
class LinkController(private val linkDao: LinkDao) {

    @GetMapping
    fun list(): List<Link> =
        linkDao.get().let { links ->
            if (hasRole(ADMIN, PRIVATE)) {
                links
            } else {
                links.map {
                    if (it.private) it.copy(title = "hidden", href = "hidden", icon = "hidden") else it
                }
            }
        }

    @Secured(ADMIN, PRIVATE)
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Link? =
        linkDao.get(id)

    @Secured(ADMIN, PRIVATE)
    @PostMapping
    fun save(
        @RequestParam id: Long?,
        @RequestParam title: String,
        @RequestParam href: String,
        @RequestParam icon: String?,
        @RequestParam target: String?,
        @RequestParam private: Boolean?,
    ): Boolean =
        linkDao.save(id, title, href, icon.nullIfBlank() ?: computeIcon(href), target.nullIfBlank(), private ?: false)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/order/{id}")
    fun setOrder(@PathVariable id: Long, @RequestParam order: Int): Boolean =
        linkDao.setOrder(id, order)

    @Secured(ADMIN, PRIVATE)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean =
        linkDao.delete(id)

    private companion object {
        fun computeIcon(href: String): String =
            URL(href).let { "${it.protocol}://${it.host}/favicon.ico" }
    }
}