package no.tomlin.api.media

import no.tomlin.api.config.ApiProperties
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import no.tomlin.api.http.HttpFetcher.Companion.useBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct

@Service
class TmdbService {

    @Autowired
    private lateinit var properties: ApiProperties

    private lateinit var fetcher: HttpFetcher
    private lateinit var posterFetcher: HttpFetcher

    @PostConstruct
    private fun init() {
        fetcher = fetcher(properties.tmdb.url)
        posterFetcher = fetcher(properties.tmdb.posterUrl)

        File(properties.cdn.poster).mkdirs()
    }

    fun fetchMedia(path: String, page: Int?): String = fetchMedia(path, mapOf("page" to (page ?: 1).toString()))

    fun fetchMedia(path: String, query: String): String = fetchMedia(path, mapOf("query" to query))

    fun fetchMedia(path: String, params: Map<String, String> = mapOf()): String =
        fetcher
            .getJson(path, mapOf(API_KEY to properties.tmdb.key).plus(params))
            .readBody()

    fun storePoster(path: String?) {
        if (path != null) {
            posterFetcher.get(path).useBody {
                File(properties.cdn.poster + path).writeBytes(it.readAllBytes())
            }
        }
    }

    companion object {
        const val API_KEY = "api_key"
    }
}