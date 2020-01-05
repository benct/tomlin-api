package no.tomlin.api.hass

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/hass")
class HassController {

    @Value("\${api.auth.key}")
    private lateinit var apiKey: String

    @Autowired
    private lateinit var hassDao: HassDao

    @GetMapping("/states")
    fun getStates(): Map<String, Any> {
        val states = hassDao.getStates()

        return mapOf(
            "temperature" to mapOf(
                "livingroom" to findValue(states, "smoke_sensor_livingroom_temperature"),
                "bathroom" to findValue(states, "flood_sensor_bathroom_temperature"),
                "kitchen" to findValue(states, "motion_hall_temperature"),
                "bedroom" to findValue(states, "netatmo_bedroom_temperature"),
                "outside" to findValue(states, "netatmo_outside_temperature"),
                "storeroom" to findValue(states, "thermometer_storeroom_temperature"),
                "office" to findValue(states, "thermometer_office_temperature")
            ),
            "consumption" to mapOf(
                "tv" to findValue(states, "power_livingroom_tv_power", "diff"),
                "pc" to findValue(states, "power_office_pc_power", "diff")
            ),
            "day" to (findValue(states, "sun") !== "below_horizon")
        )
    }

    @Secured(USER, ADMIN)
    @GetMapping("/state/{sensor}")
    fun getState(@PathVariable sensor: String): String? = hassDao.getState(sensor)

    @Secured(ADMIN)
    @PostMapping("/state")
    fun setState(@RequestParam sensor: String, @RequestParam value: String): Boolean =
        hassDao.setState(sensor, value) > 0

    @PostMapping("/set")
    fun setState(@RequestBody body: State, request: HttpServletRequest): ResponseEntity<Boolean> {
        val token = request.getHeader(AUTHORIZATION)

        return if (token == apiKey) {
            hassDao.setState(body.sensor, body.value)
            ResponseEntity.ok(true)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false)
        }
    }

    data class State(val sensor: String, val value: String)

    companion object {
        private fun findValue(states: List<Map<String, Any?>>, sensor: String, column: String = "value") =
            states.find { it["sensor"] == sensor }?.get(column)
    }
}
