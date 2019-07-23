package no.tomlin.api.admin

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController {

    @Autowired
    private lateinit var adminDao: AdminDao

    @Secured(USER, ADMIN)
    @GetMapping("/stats")
    fun getStats() = adminDao.getStats()

    @Secured(USER, ADMIN)
    @GetMapping("/visits", "/visits/{limit}")
    fun getVisits(@PathVariable limit: Int?) = adminDao.getVisits(limit ?: DEFAULT_VISITS)

    @Secured(USER, ADMIN)
    @GetMapping("/logs", "/logs/{limit}")
    fun getLogs(@PathVariable limit: Int?) = adminDao.getLogs(limit ?: DEFAULT_LOGS)

    @Secured(ADMIN)
    @PostMapping("/logs/delete")
    fun deleteLogs() = adminDao.deleteLogs()

    companion object {
        private const val DEFAULT_VISITS = 100
        private const val DEFAULT_LOGS = 25
    }
}
