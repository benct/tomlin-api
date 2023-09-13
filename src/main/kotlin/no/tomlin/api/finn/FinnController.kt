package no.tomlin.api.finn

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.PRIVATE
import no.tomlin.api.common.JsonUtils.parseJson
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/finn")
class FinnController(private val finnDao: FinnDao, private val fetcher: HttpFetcher = fetcher(FINN_URL)) {

    @Secured(ADMIN, PRIVATE)
    @Cacheable("finn")
    @GetMapping
    fun list(): Map<String, List<Map<String, Any>>> = finnDao.get()
        .fold(mutableMapOf<String, MutableList<Map<String, Any>>>()) { accumulator, entry ->
            accumulator.merge(entry["id"].toString(), mutableListOf(entry)) { oldValue, _ ->
                oldValue.add(entry)
                oldValue
            }
            accumulator
        }

    @Secured(ADMIN, PRIVATE)
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): List<Map<String, Any?>> = finnDao.get(id)

    @Secured(ADMIN, PRIVATE)
    @CacheEvict("finn", allEntries = true)
    @PostMapping
    fun track(): Boolean = trackAllPrices()

    @Secured(ADMIN, PRIVATE)
    @CacheEvict("finn", allEntries = true)
    @PostMapping("/{id}")
    fun track(@PathVariable id: Long): Boolean = finnDao.save(id, fetchPrice(id))

    @Secured(ADMIN, PRIVATE)
    @CacheEvict("finn", allEntries = true)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = finnDao.delete(id)

    @Scheduled(cron = "0 0 3,9,15,21 * * ?")
    fun trackAllPrices(): Boolean = finnDao.getUniqueIds().map { track(it) }.all { it }

    private fun fetchPrice(id: Long): String =
        fetcher.get(
            queryParams = mapOf(
                "searchkey" to "SEARCH_ID_REALESTATE_HOMES",
                "sort" to "PUBLISHED_DESC",
                "vertical" to "realestate",
                "q" to id.toString()
            )
        ).let {
            if (it.isSuccessful) {
                try {
                    val results = it.body?.string()?.parseJson<FinnResponse>()?.docs
                    if (results.isNullOrEmpty()) {
                        "Ad Not Found (404)"
                    } else {
                        results.first().price_suggestion?.amount?.toString() ?: "Price Not Found"
                    }
                } catch (e: Exception) {
                    "Fatal Parse Error"
                }
            } else "Request Error (${it.code})"
        }

    companion object {
        private const val FINN_URL = "https://www.finn.no/api/search-qf"
    }

    data class FinnResponse(val docs: List<SearchResult>)

    data class SearchResult(
        val ad_id: Long,
        val price_suggestion: Price? = null,
    )

    data class Price(val amount: Long? = null)
}