package no.tomlin.api.admin

import no.tomlin.api.admin.entity.Log
import no.tomlin.api.admin.entity.Note
import no.tomlin.api.admin.entity.Visit
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.common.PaginationResponse
import no.tomlin.api.config.ApiProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.File
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
class AdminController {

    @Autowired
    private lateinit var properties: ApiProperties

    @Autowired
    private lateinit var adminDao: AdminDao

    @Secured(USER, ADMIN)
    @GetMapping("/stats")
    fun getStats(): Map<String, Int?> = adminDao.getStats()

    @Secured(USER, ADMIN)
    @GetMapping("/visits", "/visits/{page}")
    fun getVisits(@PathVariable page: Int?): PaginationResponse<Visit> = adminDao.getVisits(page ?: 1)

    @Secured(USER, ADMIN)
    @GetMapping("/logs", "/logs/{page}")
    fun getLogs(@PathVariable page: Int?): PaginationResponse<Log> = adminDao.getLogs(page ?: 1)

    @Secured(ADMIN)
    @DeleteMapping("/logs")
    fun deleteLogs(): Boolean = adminDao.deleteLogs() > 0

    @Secured(ADMIN)
    @DeleteMapping("/logs/{id}")
    fun deleteLog(@PathVariable id: Long): Boolean = adminDao.deleteLog(id)

    @Secured(USER, ADMIN)
    @GetMapping("/notes")
    fun getNotes(): List<Note> = adminDao.getNotes()

    @Secured(ADMIN)
    @PostMapping("/notes")
    fun saveNote(@RequestParam id: Long?, @RequestParam title: String, @RequestParam content: String?): Boolean =
        adminDao.saveNote(id, title, content)

    @Secured(ADMIN)
    @DeleteMapping("/notes/{id}")
    fun deleteNote(@PathVariable id: Long): Boolean = adminDao.deleteNote(id)

    @Secured(ADMIN)
    @PostMapping("/backup")
    fun backup(): Boolean {
        val file = File("${properties.backup.path}/db_${LocalDate.now()}.sql")
        file.parentFile.mkdirs()

        val command = "mysqldump ${properties.backup.params} -r ${file.path}"
        val runtimeProcess = Runtime.getRuntime().exec(command)

        return if (runtimeProcess.waitFor() == 0) true else
            throw RuntimeException(runtimeProcess.errorStream.bufferedReader().use(BufferedReader::readText))
    }

    companion object {
        private const val DEFAULT_VISITS = 100
        private const val DEFAULT_LOGS = 25
    }
}
