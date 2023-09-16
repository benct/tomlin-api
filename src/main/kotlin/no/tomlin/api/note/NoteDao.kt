package no.tomlin.api.note

import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.TABLE_NOTE
import no.tomlin.api.note.entity.Note
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class NoteDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun getNotes(): List<Note> = jdbc.query(
        Select(from = TABLE_NOTE, orderBy = OrderBy("updated" to "DESC")),
        Note.rowMapper,
    )

    fun saveNote(id: Long?, title: String, content: String?): Boolean = jdbc.update(
        Upsert(TABLE_NOTE, mapOf("id" to id, "title" to title, "content" to content))
    )

    fun deleteNote(id: Long): Boolean = jdbc.update(
        Delete(TABLE_NOTE, Where("id" to id))
    )
}
