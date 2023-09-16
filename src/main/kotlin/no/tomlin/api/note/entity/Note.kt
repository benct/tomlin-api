package no.tomlin.api.note.entity

import org.springframework.jdbc.core.RowMapper
import java.time.LocalDateTime

data class Note(
    val id: Long,
    val title: String,
    val content: String?,
    val updated: LocalDateTime
) {

    companion object {
        val rowMapper = RowMapper<Note> { rs, _ ->
            Note(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("updated").toLocalDateTime()
            )
        }
    }
}
