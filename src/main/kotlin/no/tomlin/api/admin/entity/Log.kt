package no.tomlin.api.admin.entity

import org.springframework.jdbc.core.RowMapper
import java.time.LocalDateTime

data class Log(
    val id: Long,
    val message: String,
    val details: String?,
    val path: String?,
    val timestamp: LocalDateTime
) {
    companion object {
        val rowMapper = RowMapper<Log> { rs, _ ->
            Log(
                rs.getLong("id"),
                rs.getString("message"),
                rs.getString("details"),
                rs.getString("path"),
                rs.getTimestamp("timestamp").toLocalDateTime()
            )
        }
    }
}
