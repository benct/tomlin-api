package no.tomlin.api.hass

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.config.ApiProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/hass")
class HassController {

    @Autowired
    private lateinit var properties: ApiProperties

    @Autowired
    private lateinit var hassDao: HassDao

    @GetMapping("/states")
    fun getStates(): Map<String, Any?> {
        val states = hassDao.getStates()

        return mapOf(
            "livingroom" to findValue(states, "smoke_sensor_livingroom_temperature"),
            "livingroom_floor" to findValue(states, "netatmo_living_room_temperature"),
            "bathroom" to findValue(states, "flood_sensor_bathroom_temperature"),
            "kitchen" to findValue(states, "motion_hall_temperature"),
            "bedroom" to findValue(states, "netatmo_bedroom_temperature"),
            "outside" to findValue(states, "netatmo_outside_temperature"),
            "storeroom" to findValue(states, "thermometer_storeroom_temperature"),
            "office" to findValue(states, "thermometer_office_temperature"),
            "day" to (findValue(states, "sun") != "below_horizon")
        )
    }

    @Secured(USER, ADMIN)
    @GetMapping("/state/{sensor}")
    fun getState(@PathVariable sensor: String): String? = hassDao.getState(sensor)

    @Secured(ADMIN)
    @PostMapping("/state")
    fun setState(@RequestParam sensor: String, @RequestParam value: String): Boolean = hassDao.setState(sensor, value)

    @Secured(ADMIN)
    @GetMapping("/latest/{count}")
    fun getLatest(@PathVariable count: Int?): List<Map<String, Any?>> = hassDao.getLatest(count ?: DEFAULT_COUNT)

    @CrossOrigin("https://home.tomlin.no", "https://home.tomlin.no:8123", "http://localhost:8123")
    @PostMapping("/set")
    fun setState(@RequestBody body: State, request: HttpServletRequest): ResponseEntity<Boolean> {
        val token = request.getHeader(AUTHORIZATION)

        return if (token == properties.key) {
            hassDao.setState(body.sensor, body.value)
            ResponseEntity.ok(true)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false)
        }
    }

    data class State(val sensor: String, val value: String)

    private companion object {
        const val DEFAULT_COUNT = 50

        fun findValue(states: List<Map<String, Any?>>, sensor: String, column: String = "value") =
            states.find { it["sensor"] == sensor }?.get(column)
    }
}
