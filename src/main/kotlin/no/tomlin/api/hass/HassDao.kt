package no.tomlin.api.hass

import no.tomlin.api.common.Constants.TABLE_HASS
import no.tomlin.api.common.Extensions.checkRowsAffected
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CachePut
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.LocalDateTime

@Component
class HassDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun getState(sensor: String): String? = jdbcTemplate.queryForObject(
        "SELECT value FROM $TABLE_HASS WHERE sensor = :sensor ORDER BY updated DESC LIMIT 1",
        mapOf("sensor" to sensor),
        String::class.java
    )

    fun setState(sensor: String, value: String): Boolean = jdbcTemplate
        .update("INSERT INTO $TABLE_HASS (`sensor`, `value`) VALUES (:sensor, :value)", mapOf("sensor" to sensor, "value" to value))
        .checkRowsAffected()

    @CachePut("hass")
    fun getStates(): List<Hass> =
        jdbcTemplate.query("SELECT * FROM $TABLE_HASS WHERE `id` IN (SELECT MAX(`id`) FROM $TABLE_HASS GROUP BY `sensor`)")
        { resultSet, _ -> Hass(resultSet) }

    fun getLatest(count: Int): List<Hass> =
        jdbcTemplate.query("SELECT * FROM $TABLE_HASS ORDER BY `updated` desc LIMIT $count") { resultSet, _ -> Hass(resultSet) }

    data class Hass(val id: Long, val sensor: String, val value: String, val updated: LocalDateTime) {
        constructor(resultSet: ResultSet) : this(
            resultSet.getLong("id"),
            resultSet.getString("sensor"),
            resultSet.getString("value"),
            resultSet.getTimestamp("updated").toLocalDateTime()
        )
    }
}
