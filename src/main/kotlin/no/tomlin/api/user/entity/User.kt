package no.tomlin.api.user.entity

import java.sql.ResultSet
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
    constructor(resultSet: ResultSet) : this(
        resultSet.getString("name"),
        resultSet.getString("email"),
        null, // password should not be shown
        resultSet.getBoolean("enabled"),
        resultSet.getTimestamp("created").toLocalDateTime(),
        resultSet.getTimestamp("last_seen")?.toLocalDateTime(),
        resultSet.getString("roles").split(",")
    )
}
