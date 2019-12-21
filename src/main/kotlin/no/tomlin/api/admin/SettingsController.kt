package no.tomlin.api.admin

import no.tomlin.api.common.Constants.ADMIN
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/settings")
class SettingsController {

    @Autowired
    private lateinit var adminDao: AdminDao

    @GetMapping
    fun base(): Map<String, String?> = adminDao.getSettings()

    @GetMapping("/get")
    fun get(@RequestParam key: String): ResponseEntity<String> =
        adminDao.getSetting(key)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @Secured(ADMIN)
    @PostMapping("/set")
    fun set(@RequestParam key: String, @RequestParam value: String?): Boolean = adminDao.saveSetting(key, value) > 0
}
