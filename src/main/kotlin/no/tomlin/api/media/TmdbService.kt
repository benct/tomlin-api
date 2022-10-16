package no.tomlin.api.media

import no.tomlin.api.config.ApiProperties
import no.tomlin.api.files.FileService
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import no.tomlin.api.http.HttpFetcher.Companion.readBody
import no.tomlin.api.http.HttpFetcher.Companion.useBody
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.stereotype.Service
import java.io.File

@Service
class TmdbService(
    private val properties: ApiProperties,
    @Qualifier("GCSService") private val fileService: FileService,
) {
    private val fetcher: HttpFetcher = fetcher(BASE_URL)
    private val posterFetcher: HttpFetcher = fetcher(POSTER_URL)

    init {
        File("${properties.cdnRoot}/images/media").mkdirs()
    }

    fun fetchMedia(path: String, page: Int?): String = fetchMedia(path, mapOf("page" to (page ?: 1).toString()))

    fun fetchMedia(path: String, query: String): String = fetchMedia(path, mapOf("query" to query))

    fun fetchMedia(path: String, params: Map<String, String> = mapOf()): String =
        fetcher
            .getJson(path, mapOf(API_KEY to properties.tmdbKey).plus(params))
            .readBody()

    fun storePoster(path: String?) {
        if (path != null) {
            posterFetcher.get(path).useBody {
                fileService.store("${properties.cdnRoot}/media${path}", it, IMAGE_JPEG_VALUE)
            }
        }
    }

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val POSTER_URL = "https://image.tmdb.org/t/p/w200"
        const val API_KEY = "api_key"
    }
}