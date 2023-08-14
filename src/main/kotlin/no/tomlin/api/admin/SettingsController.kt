package no.tomlin.api.admin

import no.tomlin.api.admin.dao.AdminDao
import no.tomlin.api.common.Constants.ADMIN
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/settings")
class SettingsController(private val adminDao: AdminDao) {

    @Secured(ADMIN)
    @GetMapping
    fun get(): Map<String, Any?> = adminDao.getSettings()

    @Secured(ADMIN)
    @GetMapping("/{key}")
    fun get(@PathVariable key: String): String = adminDao.getSetting(key) ?: throw SettingNotFoundException(key)

    @Secured(ADMIN)
    @PostMapping
    fun set(@RequestParam key: String, @RequestParam value: String?): Boolean = adminDao.saveSetting(key, value)

    internal class SettingNotFoundException(key: String) : RuntimeException("Could not find setting with name $key")
}
