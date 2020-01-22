package no.tomlin.api.admin.entity

import java.sql.ResultSet
import java.time.LocalDateTime

data class Log(
    val id: Long,
    val message: String,
    val details: String?,
    val path: String?,
    val timestamp: LocalDateTime
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getLong("id"),
        resultSet.getString("message"),
        resultSet.getString("details"),
        resultSet.getString("path"),
        resultSet.getTimestamp("timestamp").toLocalDateTime()
    )
}
