package no.tomlin.api.admin.entity

import java.sql.ResultSet
import java.time.LocalDateTime

data class Visit(
    val ip: String,
    val host: String,
    val referrer: String?,
    val agent: String?,
    val page: String?,
    val visits: Int,
    val timestamp: LocalDateTime
) {
    constructor(resultSet: ResultSet) : this(
        resultSet.getString("ip"),
        resultSet.getString("host"),
        resultSet.getString("referer"),
        resultSet.getString("agent"),
        resultSet.getString("page"),
        resultSet.getInt("visits"),
        resultSet.getTimestamp("timestamp").toLocalDateTime()
    )
}
