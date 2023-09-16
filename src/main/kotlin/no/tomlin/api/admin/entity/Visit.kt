package no.tomlin.api.admin.entity

import org.springframework.jdbc.core.RowMapper
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
    companion object {
        val rowMapper = RowMapper<Visit> { rs, _ ->
            Visit(
                rs.getString("ip"),
                rs.getString("host"),
                rs.getString("referer"),
                rs.getString("agent"),
                rs.getString("page"),
                rs.getInt("visits"),
                rs.getTimestamp("timestamp").toLocalDateTime()
            )
        }
    }
}
