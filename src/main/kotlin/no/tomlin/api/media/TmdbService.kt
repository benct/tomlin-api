package no.tomlin.api.media

import no.tomlin.api.http.HttpFetcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TmdbService {

    @Value("\${api.tmdb.key}")
    private lateinit var tmdbKey: String

    @Value("\${api.tmdb.url}")
    private lateinit var tmdbUrl: String

    private lateinit var fetcher: HttpFetcher

    @PostConstruct
    private fun init() {
        fetcher = HttpFetcher.fetcher(tmdbUrl)
    }

    fun fetchMedia(path: String, page: Int?) = fetchMedia(path, mapOf("page" to (page ?: 1).toString()))

    fun fetchMedia(path: String, query: String) = fetchMedia(path, mapOf("query" to query))

    fun fetchMedia(path: String, params: Map<String, String> = mapOf()): String? =
        fetcher
            .getJson(path, mapOf("api_key" to tmdbKey).plus(params))
            .use {
                if (it.isSuccessful) {
                    it.body()?.string()
                } else null
            }
}