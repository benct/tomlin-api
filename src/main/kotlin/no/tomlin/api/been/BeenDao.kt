package no.tomlin.api.been

import no.tomlin.api.common.Constants.TABLE_BEEN
import no.tomlin.api.common.Extensions.checkRowsAffected
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class BeenDao(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun get(): List<Map<String, Any>> = jdbcTemplate.query(
        "SELECT * FROM $TABLE_BEEN", EmptySqlParameterSource.INSTANCE
    ) { resultSet, _ ->
        mapOf(
            "country" to resultSet.getString("country"),
            "name" to resultSet.getString("name"),
            "visited" to resultSet.getInt("visited"),
        )
    }

    fun add(country: String, name: String): Boolean = jdbcTemplate.update(
        "INSERT INTO $TABLE_BEEN (`country`, `name`) VALUES (:country, :name)",
        mapOf("country" to country, "name" to name)
    ).checkRowsAffected()

    fun remove(country: String): Boolean = jdbcTemplate.update(
        "DELETE FROM $TABLE_BEEN WHERE `country` = :country",
        mapOf("country" to country)
    ).checkRowsAffected()

    fun increment(country: String): Boolean = jdbcTemplate.update(
        "UPDATE $TABLE_BEEN SET `visited` = `visited` + 1 WHERE `country` = :country",
        mapOf("country" to country)
    ).checkRowsAffected()

    fun decrement(country: String): Boolean = jdbcTemplate.update(
        "UPDATE $TABLE_BEEN SET `visited` = `visited` - 1 WHERE `country` = :country",
        mapOf("country" to country)
    ).checkRowsAffected()
}
