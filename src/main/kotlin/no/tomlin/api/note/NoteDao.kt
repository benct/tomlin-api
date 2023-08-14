package no.tomlin.api.note

import no.tomlin.api.common.Constants.TABLE_NOTE
import no.tomlin.api.common.Extensions.checkRowsAffected
import no.tomlin.api.note.entity.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class NoteDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun getNotes(): List<Note> =
        jdbcTemplate.query("SELECT * FROM $TABLE_NOTE ORDER BY updated DESC") { resultSet, _ -> Note(resultSet) }

    fun saveNote(id: Long?, title: String, content: String?): Boolean =
        jdbcTemplate.update(
            "INSERT INTO $TABLE_NOTE (id, title, content) VALUES (:id, :title, :content) " +
                "ON DUPLICATE KEY UPDATE title = :title, content = :content",
            mapOf("id" to id, "title" to title, "content" to content)
        ).checkRowsAffected()

    fun deleteNote(id: Long): Boolean = jdbcTemplate
        .update("DELETE FROM $TABLE_NOTE WHERE id = :id", mapOf("id" to id))
        .checkRowsAffected()
}
