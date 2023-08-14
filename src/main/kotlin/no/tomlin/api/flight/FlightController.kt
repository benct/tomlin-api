package no.tomlin.api.flight

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.flight.entity.Flight
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/flight")
class FlightController {

    @Autowired
    private lateinit var flightDao: FlightDao

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(): List<List<Flight>> {
        val grouped = mutableMapOf<String, MutableList<Flight>>()

        flightDao.getFlights().forEach {
            grouped.putIfAbsent(it.reference, mutableListOf(it))?.add(it)
        }

        return grouped.values
            .sortedByDescending { it.first().departure }
            .toList()
    }

    @Secured(ADMIN)
    @PostMapping
    fun store(
        @RequestParam id: Long?,
        @RequestParam origin: String,
        @RequestParam destination: String,
        @RequestParam departure: String,
        @RequestParam arrival: String,
        @RequestParam carrier: String,
        @RequestParam number: String,
        @RequestParam cabin: String?,
        @RequestParam aircraft: String?,
        @RequestParam seat: String?,
        @RequestParam reference: String,
        @RequestParam info: String?
    ): Boolean =
        flightDao.saveFlight(
            Flight(id, origin, destination, departure, arrival, carrier, number, cabin, aircraft, seat, reference, info)
        )

    @Secured(ADMIN)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = flightDao.deleteFlight(id)
}
