package no.tomlin.api.admin.dao

import no.tomlin.api.admin.entity.User
import no.tomlin.api.common.Constants.TABLE_ROLE
import no.tomlin.api.common.Constants.TABLE_USER
import no.tomlin.api.common.Extensions.checkRowsAffected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun updateLastSeen(email: String) =
        jdbcTemplate.update(
            "UPDATE $TABLE_USER SET last_seen = CURRENT_TIMESTAMP() WHERE email = :email",
            mapOf("email" to email)
        ).checkRowsAffected()

    fun getUser(email: String): User? =
        jdbcTemplate.query(
            "SELECT u.name, u.email, u.enabled, u.created, u.last_seen, GROUP_CONCAT(r.role) as roles " +
                "FROM $TABLE_USER u JOIN $TABLE_ROLE r ON u.email = r.email " +
                "WHERE u.email = :email",
            mapOf("email" to email)
        ) { resultSet, _ -> User(resultSet) }.firstOrNull()

    fun getUsers(): List<User> =
        jdbcTemplate.query(
            "SELECT u.name, u.email, u.enabled, u.created, u.last_seen, GROUP_CONCAT(r.role) as roles " +
                "FROM $TABLE_USER u JOIN $TABLE_ROLE r ON u.email = r.email " +
                "GROUP BY u.email ORDER BY u.name"
        ) { resultSet, _ -> User(resultSet) }

    fun storeUser(name: String, email: String, enabled: Boolean = true, password: String? = null): Boolean =
        jdbcTemplate.update(
            "INSERT INTO $TABLE_USER (name, email, password) VALUES (:name, :email, :password) " +
                "ON DUPLICATE KEY UPDATE name = :name, email = :email, enabled = :enabled" +
                password?.let { ", password = :password" }.orEmpty(),
            mapOf("name" to name, "email" to email, "enabled" to enabled, "password" to password)
        ).checkRowsAffected()

    fun deleteUser(email: String): Boolean =
        jdbcTemplate.update(
            "DELETE FROM $TABLE_USER WHERE email = :email",
            mapOf("email" to email)
        ).checkRowsAffected()

    fun storeRole(email: String, role: String): Boolean =
        jdbcTemplate.update(
            "INSERT INTO $TABLE_ROLE (email, role) VALUES (:email, :role) ON DUPLICATE KEY UPDATE email = email",
            mapOf("email" to email, "role" to role)
        ).checkRowsAffected()

    fun deleteRole(email: String, role: String): Boolean =
        jdbcTemplate.update(
            "DELETE FROM $TABLE_ROLE WHERE email = :email AND role = :role",
            mapOf("email" to email, "role" to role)
        ).checkRowsAffected()

    fun deleteRoles(email: String): Boolean =
        jdbcTemplate.update("DELETE FROM $TABLE_ROLE WHERE email = :email", mapOf("email" to email)) > 0
}
