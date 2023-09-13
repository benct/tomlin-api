package no.tomlin.api.been

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.PRIVATE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/been")
class BeenController(private val beenDao: BeenDao) {

    @GetMapping("/countries")
    fun allCountries(): List<Map<String, String>> =
        Locale.getISOCountries().map { iso ->
            Locale.of("", iso).let {
                mapOf(
                    "iso2" to it.country,
                    "iso3" to it.isO3Country,
                    "name" to it.displayCountry,
                )
            }
        }

    @Cacheable("been")
    @GetMapping
    fun getCountries(): List<Map<String, Any>> = beenDao.get()

    @Secured(ADMIN, PRIVATE)
    @CacheEvict("been", allEntries = true)
    @PostMapping("/{country}")
    fun addCountry(@PathVariable country: String) = beenDao.add(country, Locale.of("", country).displayCountry)

    @Secured(ADMIN, PRIVATE)
    @CacheEvict("been", allEntries = true)
    @DeleteMapping("/{country}")
    fun removeCountry(@PathVariable country: String) = beenDao.remove(country)

    @Secured(ADMIN, PRIVATE)
    @CacheEvict("been", allEntries = true)
    @PostMapping("/increment/{country}")
    fun increment(@PathVariable country: String) = beenDao.increment(country)

    @Secured(ADMIN, PRIVATE)
    @CacheEvict("been", allEntries = true)
    @PostMapping("/decrement/{country}")
    fun decrement(@PathVariable country: String) = beenDao.decrement(country)
}