package no.tomlin.api.note

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.note.entity.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/note")
class NoteController {

    @Autowired
    private lateinit var noteDao: NoteDao

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(): List<Note> = noteDao.getNotes()

    @Secured(ADMIN)
    @PostMapping
    fun store(@RequestParam id: Long?, @RequestParam title: String, @RequestParam content: String?): Boolean =
        noteDao.saveNote(id, title, content)

    @Secured(ADMIN)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = noteDao.deleteNote(id)
}