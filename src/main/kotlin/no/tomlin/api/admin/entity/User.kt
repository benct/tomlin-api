package no.tomlin.api.admin.entity

import org.springframework.jdbc.core.RowMapper
import java.time.LocalDateTime

data class User(
    val name: String,
    val email: String,
    val password: String? = null,
    val enabled: Boolean = true,
    val created: LocalDateTime,
    val lastSeen: LocalDateTime? = null,
    val roles: List<String> = emptyList()
) {
    companion object {
        val rowMapper = RowMapper<User> { rs, _ ->
            User(
                rs.getString("name"),
                rs.getString("email"),
                null, // password should not be shown
                rs.getBoolean("enabled"),
                rs.getTimestamp("created").toLocalDateTime(),
                rs.getTimestamp("last_seen")?.toLocalDateTime(),
                rs.getString("roles").split(",")
            )
        }
    }
}
