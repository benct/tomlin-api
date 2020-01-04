package no.tomlin.api.admin.entity

import java.sql.ResultSet
import java.time.LocalDateTime

data class Log(
    val message: String,
    val details: String?,
    val path: String?,
    val timestamp: LocalDateTime
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getString("message"),
        resultSet.getString("details"),
        resultSet.getString("path"),
        resultSet.getTimestamp("timestamp").toLocalDateTime()
    )
}
