package no.tomlin.api.admin

import no.tomlin.api.admin.entity.Log
import no.tomlin.api.admin.entity.Note
import no.tomlin.api.admin.entity.Visit
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.File
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
class AdminController {

    @Value("\${api.db.backup}")
    private lateinit var backupParams: String

    @Autowired
    private lateinit var adminDao: AdminDao

    @Secured(USER, ADMIN)
    @GetMapping("/stats")
    fun getStats(): Map<String, Int?> = adminDao.getStats()

    @Secured(USER, ADMIN)
    @GetMapping("/visits", "/visits/{limit}")
    fun getVisits(@PathVariable limit: Int?): List<Visit> = adminDao.getVisits(limit ?: DEFAULT_VISITS)

    @Secured(USER, ADMIN)
    @GetMapping("/logs", "/logs/{limit}")
    fun getLogs(@PathVariable limit: Int?): List<Log> = adminDao.getLogs(limit ?: DEFAULT_LOGS)

    @Secured(ADMIN)
    @DeleteMapping("/logs")
    fun deleteLogs(): Boolean = adminDao.deleteLogs() > 0

    @Secured(USER, ADMIN)
    @GetMapping("/notes")
    fun getNotes(): List<Note> = adminDao.getNotes()

    @Secured(ADMIN)
    @PostMapping("/notes")
    fun saveNote(@RequestParam id: Int?, @RequestParam title: String, @RequestParam content: String?): Boolean =
        adminDao.saveNote(id, title, content) == 1

    @Secured(ADMIN)
    @DeleteMapping("/notes/{id}")
    fun deleteNote(@PathVariable id: Int): Boolean = adminDao.deleteNote(id) == 1

    @Secured(ADMIN)
    @PostMapping("/backup")
    fun backup(): Boolean {
        val file = File("backup/db_${LocalDate.now()}.sql")
        file.parentFile.mkdirs()

        val command = "mysqldump $backupParams -r ${file.path}"
        val runtimeProcess = Runtime.getRuntime().exec(command)

        return if (runtimeProcess.waitFor() == 0) true else
            throw RuntimeException(runtimeProcess.errorStream.bufferedReader().use(BufferedReader::readText))
    }

    companion object {
        private const val DEFAULT_VISITS = 100
        private const val DEFAULT_LOGS = 25
    }
}
