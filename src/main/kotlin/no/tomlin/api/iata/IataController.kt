package no.tomlin.api.iata

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Extensions.cleanBlank
import no.tomlin.api.entity.Airline
import no.tomlin.api.entity.Location
import no.tomlin.api.http.HttpFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/iata")
class IataController {

    @Autowired
    private lateinit var iataDao: IataDao

    private val fetcher = HttpFetcher.fetcher(OPTD_URL)

    @Secured(ADMIN)
    @PostMapping("/airlines")
    fun updateAirlines(): Int {
        fetcher.get(AIRLINE_PATH).use { response ->
            if (response.isSuccessful) {
                iataDao.deleteAirlines()

                val airlines = parseCsv(response.body()?.string()).map(::Airline)

                return iataDao.batchAirlines(airlines).size
            }
        }
        return 0
    }

    @Secured(ADMIN)
    @PostMapping("/locations")
    fun updateLocations(): Int {
        fetcher.get(LOCATION_PATH).use { response ->
            if (response.isSuccessful) {
                iataDao.deleteLocations()

                val locations = parseCsv(response.body()?.string()).map(::Location)

                return iataDao.batchLocations(locations).size
            }
        }
        return 0
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
            .map { it.split('^').cleanBlank() }
    }
}
