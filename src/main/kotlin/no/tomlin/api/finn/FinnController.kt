package no.tomlin.api.finn

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.JsonUtils.parseJson
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/finn")
class FinnController(private val fetcher: HttpFetcher = fetcher(FINN_URL)) {

    @Autowired
    private lateinit var finnDao: FinnDao

    @GetMapping
    fun list(): List<Map<String, Any?>> = finnDao.get()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): List<Map<String, Any?>> = finnDao.get(id)

    @Secured(ADMIN)
    @PostMapping
    fun track(): Boolean = finnDao.getUniqueIds().map { track(it) }.all { it }

    @PostMapping("/{id}")
    fun track(@PathVariable id: Long): Boolean = finnDao.save(id, fetchPrice(id))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = finnDao.delete(id)

    private fun fetchPrice(id: Long): String =
        fetcher.get(queryParams = mapOf(
            "vertical" to "realestate",
            "subvertical" to "homes",
            "q" to id.toString())
        ).let {
            if (it.isSuccessful) {
                val results = it.body?.string()?.parseJson<FinnResponse>()?.displaySearchResults
                if (results.isNullOrEmpty()) {
                    "Not Found (404)"
                } else {
                    results.first().bodyRow.lastOrNull() ?: "Parse Error"
                }
            } else "Request Error (${it.code})"
        }

    companion object {
        private const val FINN_URL = "https://www.finn.no/api/search"
    }

    data class FinnResponse(val displaySearchResults: List<SearchResult>)

    data class SearchResult(
        val adId: Long,
        val adUrl: String,
        val imageUrl: String? = null,
        val topRowCenter: String? = null,
        val topRowLeft: String? = null,
        val topRowRight: String? = null,
        val titleRow: String,
        val bodyRow: List<String>,
        val bottomRow1: String? = null,
        val bottomRow2: String? = null,
        val bottomRow3: String? = null
    )
}