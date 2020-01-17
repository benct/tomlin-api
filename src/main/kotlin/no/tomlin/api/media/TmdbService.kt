package no.tomlin.api.media

import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import no.tomlin.api.http.HttpFetcher.Companion.useBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct

@Service
class TmdbService {

    @Value("\${api.tmdb.key}")
    private lateinit var tmdbKey: String

    @Value("\${api.tmdb.url}")
    private lateinit var tmdbUrl: String

    @Value("\${api.tmdb.img}")
    private lateinit var tmdbImg: String

    private lateinit var fetcher: HttpFetcher
    private lateinit var posterFetcher: HttpFetcher

    @PostConstruct
    private fun init() {
        fetcher = HttpFetcher.fetcher(tmdbUrl)
        posterFetcher = HttpFetcher.fetcher(tmdbImg)

        File(POSTER_PATH).mkdirs()
    }

    fun fetchMedia(path: String, page: Int?): String = fetchMedia(path, mapOf("page" to (page ?: 1).toString()))

    fun fetchMedia(path: String, query: String): String = fetchMedia(path, mapOf("query" to query))

    fun fetchMedia(path: String, params: Map<String, String> = mapOf()): String =
        fetcher
            .getJson(path, mapOf(API_KEY to tmdbKey).plus(params))
            .readBody()

    fun storePoster(path: String?) {
        if (path != null) {
            posterFetcher.get(path).useBody {
                File(POSTER_PATH + path).writeBytes(it.readAllBytes())
            }
        }
    }

    companion object {
        const val API_KEY = "api_key"
        const val POSTER_PATH = "/var/www/html/images/media"
    }
}