package no.tomlin.api.note

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.PRIVATE
import no.tomlin.api.note.entity.Note
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/note")
class NoteController(private val noteDao: NoteDao) {

    @Secured(ADMIN, PRIVATE)
    @GetMapping
    fun get(): List<Note> = noteDao.getNotes()

    @Secured(ADMIN, PRIVATE)
    @PostMapping
    fun store(@RequestParam id: Long?, @RequestParam title: String, @RequestParam content: String?): Boolean =
        noteDao.saveNote(id, title, content)

    @Secured(ADMIN, PRIVATE)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = noteDao.deleteNote(id)
}