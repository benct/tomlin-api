package no.tomlin.api.entity

import java.sql.ResultSet
import java.time.LocalDateTime

data class Note(
    val id: Int,
    val title: String,
    val content: String?,
    val updated: LocalDateTime
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getInt("id"),
        resultSet.getString("title"),
        resultSet.getString("content"),
        resultSet.getTimestamp("updated").toLocalDateTime()
    )
}
