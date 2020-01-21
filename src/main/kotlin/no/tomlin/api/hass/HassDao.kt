package no.tomlin.api.hass

import no.tomlin.api.common.Constants.TABLE_HASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class HassDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun getState(sensor: String): String? = jdbcTemplate.queryForObject(
        "SELECT value FROM $TABLE_HASS WHERE sensor = :sensor ORDER BY updated DESC LIMIT 1",
        mapOf("sensor" to sensor),
        String::class.java
    )

    fun setState(sensor: String, value: String): Int = jdbcTemplate.update(
        "INSERT INTO $TABLE_HASS (`sensor`, `value`) VALUES (:sensor, :value)",
        mapOf("sensor" to sensor, "value" to value)
    )

    fun getStates(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        "SELECT `sensor`, `value` FROM $TABLE_HASS WHERE `id` IN (SELECT MAX(`id`) FROM $TABLE_HASS GROUP BY `sensor`)",
        EmptySqlParameterSource.INSTANCE
    )

    fun getLatest(count: Int): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        "SELECT * FROM $TABLE_HASS ORDER BY desc LIMIT $count",
        EmptySqlParameterSource.INSTANCE
    )
}
