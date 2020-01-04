package no.tomlin.api.media

import no.tomlin.api.http.HttpFetcher
import okhttp3.Response
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
    }

    fun fetchMedia(path: String, page: Int?): String = fetchMedia(path, mapOf("page" to (page ?: 1).toString()))

    fun fetchMedia(path: String, query: String): String = fetchMedia(path, mapOf("query" to query))

    fun fetchMedia(path: String, params: Map<String, String> = mapOf()): String =
        fetcher
            .getJson(path, mapOf(API_KEY to tmdbKey).plus(params))
            .use {
                if (it.isSuccessful) {
                    it.body()?.string() ?: throw EmptyResponseException(it)
                } else throw UnsuccessfulResponseException(it)
            }

    fun storePoster(path: String?) =
        path?.let {
            posterFetcher.get(path).use {
                if (it.isSuccessful) {
                    it.body()?.bytes()?.let {
                        File(POSTER_PATH + path).writeBytes(it)
                    }
                } else throw UnsuccessfulResponseException(it)
            }
        }

    companion object {
        const val API_KEY = "api_key"
        const val POSTER_PATH = "assets/images/media"
    }

    internal class EmptyResponseException(response: Response) :
        RuntimeException("Empty response (${response.code()}) from TMDB (${response.request().url().encodedPath()})")

    internal class UnsuccessfulResponseException(response: Response) :
        RuntimeException("Invalid response (${response.code()}) from TMDB (${response.request().url().encodedPath()}): ${response.message()}")
}