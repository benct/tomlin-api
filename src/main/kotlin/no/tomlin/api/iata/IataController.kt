package no.tomlin.api.iata

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import no.tomlin.api.iata.entity.Airline
import no.tomlin.api.iata.entity.Location
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/iata")
class IataController {

    @Autowired
    private lateinit var iataDao: IataDao

    private val fetcher = HttpFetcher.fetcher(OPTD_URL)

    @GetMapping("/search/{query}")
    fun search(@PathVariable query: String, response: HttpServletResponse): List<Any> {
        val value = query.trim().toUpperCase()

        val results = when (value.length) {
            2 -> iataDao.getAirlines(value)
            3 -> iataDao.getLocations(value)
            else -> emptyList()
        }

        if (results.isEmpty()) {
            response.status = NO_CONTENT.value()
        }
        return results
    }

    @Secured(ADMIN)
    @PostMapping("/airlines")
    fun updateAirlines(): Int =
        fetcher.get(AIRLINE_PATH).readBody().let {
            iataDao.deleteAirlines()

            val airlines = parseCsv(it).map(::Airline)

            return iataDao.batchAirlines(airlines).size
        }

    @Secured(ADMIN)
    @PostMapping("/locations")
    fun updateLocations(): Int =
        fetcher.get(LOCATION_PATH).readBody().let {
            iataDao.deleteLocations()

            val locations = parseCsv(it).map(::Location)

            iataDao.batchLocations(locations).size
        }

    companion object {
        private const val OPTD_URL = "https://raw.githubusercontent.com/opentraveldata/opentraveldata/master/opentraveldata"
        private const val AIRLINE_PATH = "/optd_airline_best_known_so_far.csv"
        private const val LOCATION_PATH = "/optd_por_public.csv"

        private fun parseCsv(body: String?) = body
            ?.trim()
            ?.split('\n')
            .orEmpty()
            .drop(1) // Skip header row
            .map { it.split('^').nullIfBlank() }
    }
}
