package no.tomlin.api.admin.dao

import no.tomlin.api.admin.entity.User
import no.tomlin.api.db.*
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Table.TABLE_ROLE
import no.tomlin.api.db.Table.TABLE_USER
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun updateLastSeen(email: String) = jdbc.update(
        Update(TABLE_USER, mapOf("last_seen" to "CURRENT_TIMESTAMP()"), Where("email" to email))
    )

    fun getUser(email: String): User? = jdbc.queryForObject(
        Select(
            columns = "u.name, u.email, u.enabled, u.created, u.last_seen, GROUP_CONCAT(r.role) AS roles",
            from = TABLE_USER,
            join = Join(TABLE_ROLE, "u.email" to "r.email"),
            where = Where("email" to email),
            groupBy = GroupBy("email")
        ),
        User.rowMapper,
    )

    fun getUsers(): List<User> = jdbc.query(
        Select(
            columns = "u.name, u.email, u.enabled, u.created, u.last_seen, GROUP_CONCAT(r.role) AS roles",
            from = TABLE_USER,
            join = Join(TABLE_ROLE, "u.email" to "r.email"),
            groupBy = GroupBy("u.email"),
            orderBy = OrderBy("u.name"),
        ),
        User.rowMapper,
    )

    fun storeUser(name: String, email: String, enabled: Boolean, password: String? = null): Boolean = jdbc.update(
        Upsert(
            TABLE_USER,
            mapOf("name" to name, "email" to email, "enabled" to enabled, "password" to password)
                .filterValues { it != null }
        )
    )

    fun deleteUser(email: String): Boolean = jdbc.update(
        Delete(TABLE_USER, Where("email" to email))
    )

    fun storeRole(email: String, role: String): Boolean = jdbc.update(
        Upsert(TABLE_ROLE, mapOf("email" to email, "role" to role))
    )

    fun deleteRole(email: String, role: String): Boolean = jdbc.update(
        Delete(TABLE_ROLE, Where("email" to email, "role" to role))
    )

    fun deleteRoles(email: String): Boolean = jdbc.update(
        Delete(TABLE_ROLE, Where("email" to email))
    )
}
