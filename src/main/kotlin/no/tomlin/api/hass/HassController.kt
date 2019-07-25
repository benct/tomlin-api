package no.tomlin.api.hass

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/hass")
class HassController {

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
                "tv" to findValue(states, "power_livingroom_tv_power_2", "diff"),
                "pc" to findValue(states, "power_office_pc_power_2", "diff")
            ),
            "day" to (findValue(states, "sun") !== "below_horizon")
        )
    }

    @Secured(USER, ADMIN)
    @GetMapping("/state/{sensor}")
    fun getState(@PathVariable sensor: String) = hassDao.getState(sensor)

    @Secured(ADMIN)
    @PostMapping("/state")
    fun setState(@RequestParam sensor: String, @RequestParam value: String) = hassDao.setState(sensor, value)

    companion object {
        private fun findValue(states: List<Map<String, Any?>>, sensor: String, column: String = "value") =
            states.find { it["sensor"] == sensor }?.get(column)
    }
}
