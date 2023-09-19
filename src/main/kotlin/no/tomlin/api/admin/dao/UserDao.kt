package no.tomlin.api.admin.dao

import no.tomlin.api.admin.entity.User
import no.tomlin.api.db.Delete
import no.tomlin.api.db.Extensions.query
import no.tomlin.api.db.Extensions.queryForObject
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Select
import no.tomlin.api.db.Table.TABLE_ROLE
import no.tomlin.api.db.Table.TABLE_USER
import no.tomlin.api.db.Update
import no.tomlin.api.db.Upsert
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun updateLastSeen(email: String) = jdbc.update(
        Update(TABLE_USER).setRaw("last_seen" to "CURRENT_TIMESTAMP()").where("email").eq(email)
    )

    fun getUser(email: String): User? = jdbc.queryForObject(
        Select(TABLE_USER)
            .columns("name", "email", "enabled", "created", "last_seen")
            .column(TABLE_ROLE, "role").groupConcat("roles")
            .join(TABLE_ROLE).on("email", "email")
            .where("email").eq(email)
            .groupBy("email"),
        User.rowMapper,
    )

    fun getUsers(): List<User> = jdbc.query(
        Select(TABLE_USER)
            .columns("name", "email", "enabled", "created", "last_seen")
            .column(TABLE_ROLE, "role").groupConcat("roles")
            .join(TABLE_ROLE).on("email", "email")
            .groupBy("email")
            .orderBy("name"),
        User.rowMapper,
    )

    fun storeUser(name: String, email: String, enabled: Boolean, password: String? = null): Boolean = jdbc.update(
        Upsert(TABLE_USER).data(
            mapOf("name" to name, "email" to email, "enabled" to enabled, "password" to password)
                .filterValues { it != null }
        )
    )

    fun deleteUser(email: String): Boolean = jdbc.update(
        Delete(TABLE_USER).where("email").eq(email)
    )

    fun storeRole(email: String, role: String): Boolean = jdbc.update(
        Upsert(TABLE_ROLE).data("email" to email, "role" to role)
    )

    fun deleteRole(email: String, role: String): Boolean = jdbc.update(
        Delete(TABLE_ROLE)
            .where("email").eq(email)
            .and("role").eq(role)
    )

    fun deleteRoles(email: String): Boolean = jdbc.update(
        Delete(TABLE_ROLE).where("email").eq(email)
    )
}
