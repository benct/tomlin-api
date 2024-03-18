package no.tomlin.api.iata

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import no.tomlin.api.iata.entity.Airline
import no.tomlin.api.iata.entity.Location
import no.tomlin.api.logging.LogDao
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/iata")
class IataController(
    private val iataDao: IataDao,
    private val logger: LogDao,
    private val fetcher: HttpFetcher = fetcher(OPTD_URL)
) {

    @CrossOrigin("https://benct.github.io")
    @Cacheable("iataSearch")
    @GetMapping("/search/{query}")
    fun search(@PathVariable query: String): ResponseEntity<List<Any>> {
        val value = query.trim().uppercase()

        val results = when (value.length) {
            2 -> iataDao.getAirlines(value)
            3 -> iataDao.getLocations(value)
            else -> emptyList()
        }

        if (results.isEmpty()) {
            return ResponseEntity.status(NO_CONTENT).body(results)
        }
        return ResponseEntity.ok(results)
    }

    @Cacheable("iataAirlineSearch")
    @GetMapping("/search/airline/{query}")
    fun searchAirline(
        @PathVariable query: String,
        @RequestParam airlineOnly: Boolean?,
        @RequestParam limit: Int?
    ): ResponseEntity<List<SearchOption>> {
        val value = query.trim()
        val results = iataDao.searchAirlines(value)
            .sortedByDescending { it.matchesIataIcao(value) }
            .filter { if (airlineOnly == true) it.isAirline else true }
            .map { SearchOption(it) }

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.ok(results)
    }

    @Cacheable("iataLocationSearch")
    @GetMapping("/search/location/{query}")
    fun searchLocation(
        @PathVariable query: String,
        @RequestParam airportOnly: Boolean?,
        @RequestParam matchIdentifier: Boolean?,
        @RequestParam limit: Int?
    ): ResponseEntity<List<SearchOption>> {
        val value = query.trim()
        val locations = if (matchIdentifier == true) iataDao.getLocations(value) else iataDao.searchLocations(value)
        val results = locations
            .filter { if (airportOnly == true) it.isAirport else true }
            .groupByMetropolitan()
            .sortedByDescending { it.first().matchesIataIcao(value) }
            .flatMap { it.sortByMetropolitanAndAirport().mapIndexed { idx, loc -> SearchOption(loc, idx) } }

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.ok(results)
    }

    @Secured(ADMIN)
    @CacheEvict(value = ["iataSearch", "iataAirlineSearch"], allEntries = true)
    @PostMapping("/airlines")
    fun updateAirlines(): Int =
        fetcher.get(AIRLINE_PATH).readBody().let {
            iataDao.deleteAirlines()

            val airlines = parseCsv(it).map(::Airline)

            iataDao.batchAirlines(airlines).size
                .also { logger.info("IATA", "Updated $it airlines") }
        }

    @Secured(ADMIN)
    @CacheEvict(value = ["iataSearch", "iataLocationSearch"], allEntries = true)
    @PostMapping("/locations")
    fun updateLocations(): Int =
        fetcher.get(LOCATION_PATH).readBody().let {
            iataDao.deleteLocations()

            val locations = parseCsv(it).map(::Location)

            iataDao.batchLocations(locations).size
                .also { logger.info("IATA", "Updated $it locations") }
        }

    data class SearchOption(
        val value: String,
        val text: String,
        val subText: String? = null,
        val indent: Boolean = false,
        val data: Any,
    ) {
        constructor(airline: Airline) : this(
            value = airline.iataCode,
            text = "${airline.name} (${airline.iataCode})",
            subText = airline.typeName,
            data = airline,
        )

        constructor(location: Location, idx: Int) : this(
            value = location.iataCode,
            text = "${location.name} (${location.iataCode})",
            subText = "${location.cityName} (${location.cityCode})${location.area?.let { ", $it" }.orEmpty()}, ${location.country}",
            indent = idx > 0 && location.type != "C",
            data = location,
        )
    }

    private companion object {
        const val OPTD_URL = "https://raw.githubusercontent.com/opentraveldata/opentraveldata/master/opentraveldata"
        const val AIRLINE_PATH = "/optd_airline_best_known_so_far.csv"
        const val LOCATION_PATH = "/optd_por_public.csv"

        fun parseCsv(body: String?) = body
            ?.trim()
            ?.split('\n')
            .orEmpty()
            .drop(1) // Skip header row
            .map { it.split('^').nullIfBlank() }

        fun Collection<Location>.groupByMetropolitan(): Collection<List<Location>> =
            this.groupBy { it.cityCode ?: it.iataCode }.values

        fun Collection<Location>.sortByMetropolitanAndAirport() =
            this.sortedBy { if (it.type == "C") "0-${it.iataCode}" else if (it.isAirport) "2-${it.iataCode}" else it.iataCode }

        fun Location.matchesIataIcao(query: String) =
            query.equals(iataCode, true) || query.equals(cityCode, true) || query.equals(icaoCode, true)

        fun Airline.matchesIataIcao(query: String) =
            query.equals(iataCode, true) || query.equals(icaoCode, true)
    }
}
