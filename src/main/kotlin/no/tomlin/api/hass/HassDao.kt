package no.tomlin.api.hass

import no.tomlin.api.common.Constants.TABLE_HASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.ColumnMapRowMapper
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

    fun getStates(): List<Map<String, Any?>> = jdbcTemplate.query(
        "SELECT h.sensor, h.value, " +
            "FORMAT(h.value - " +
                "(SELECT f.value FROM $TABLE_HASS f " +
                "WHERE f.updated > DATE_SUB(NOW(), INTERVAL 1 MONTH) AND f.sensor=h.sensor GROUP BY f.sensor), 1) diff " +
            "FROM $TABLE_HASS h " +
            "WHERE h.id IN (SELECT MAX(m.id) FROM $TABLE_HASS m GROUP BY m.sensor)",
        ColumnMapRowMapper()
    )
}
