package no.tomlin.api.admin

import no.tomlin.api.admin.dao.AdminDao
import no.tomlin.api.admin.entity.Note
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/note")
class NoteController {

    @Autowired
    private lateinit var adminDao: AdminDao

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(): List<Note> = adminDao.getNotes()

    @Secured(ADMIN)
    @PostMapping
    fun store(@RequestParam id: Long?, @RequestParam title: String, @RequestParam content: String?): Boolean =
        adminDao.saveNote(id, title, content)

    @Secured(ADMIN)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = adminDao.deleteNote(id)
}