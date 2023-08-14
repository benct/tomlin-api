package no.tomlin.api.note.entity

import java.sql.ResultSet
import java.time.LocalDateTime

data class Note(
    val id: Long,
    val title: String,
    val content: String?,
    val updated: LocalDateTime
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getLong("id"),
        resultSet.getString("title"),
        resultSet.getString("content"),
        resultSet.getTimestamp("updated").toLocalDateTime()
    )
}
