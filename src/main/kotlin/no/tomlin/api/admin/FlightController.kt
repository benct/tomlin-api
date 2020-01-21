package no.tomlin.api.admin

import no.tomlin.api.admin.entity.Flight
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/flight")
class FlightController {

    @Autowired
    private lateinit var adminDao: AdminDao

    @Secured(USER, ADMIN)
    @GetMapping
    fun get(): List<List<Flight>> {
        val grouped = mutableMapOf<String, MutableList<Flight>>()

        adminDao.getFlights().forEach {
            grouped.putIfAbsent(it.reference, mutableListOf(it))?.add(it)
        }

        return grouped.values
            .sortedByDescending { it.first().departure }
            .toList()
    }

    @Secured(ADMIN)
    @PostMapping
    fun save(
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
        adminDao.saveFlight(
            Flight(id, origin, destination, departure, arrival, carrier, number, cabin, aircraft, seat, reference, info)
        ) == 1

    @Secured(ADMIN)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): Boolean = adminDao.deleteFlight(id) == 1
}
