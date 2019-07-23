package no.tomlin.api.entity

import java.sql.ResultSet
import java.time.LocalDateTime

data class Log(
    val message: String,
    val details: String?,
    val timestamp: LocalDateTime
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getString("message"),
        resultSet.getString("details"),
        resultSet.getTimestamp("timestamp").toLocalDateTime()
    )
}
