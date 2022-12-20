package no.tomlin.api.http

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import org.springframework.http.ResponseEntity
import java.io.InputStream
import java.net.MalformedURLException
import java.time.Duration
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds
import java.util.concurrent.TimeUnit.MILLISECONDS

open class HttpFetcher private constructor(private val baseUrl: String?, timeout: Duration = REQUEST_TIMEOUT) {

    private var client = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(MAX_TOTAL_CONNECTIONS, CONNECTIONPOOL_KEEPALIVE.toMillis(), MILLISECONDS))
        .connectTimeout(CONNECTION_TIMEOUT.toMillis(), MILLISECONDS)
        .readTimeout(timeout.toMillis(), MILLISECONDS)
        .writeTimeout(timeout.toMillis(), MILLISECONDS)
        .build()

    open fun get(path: String = "", headers: Map<String, String> = mapOf(), queryParams: Map<String, String?> = mapOf()) =
        fetch(path = path, headers = headers, queryParams = queryParams.mapValues { listOf(it.value) })

    open fun getJson(path: String = "", queryParams: Map<String, String?> = mapOf()) =
        get(path = path, headers = mapOf("Accept" to "application/json"), queryParams = queryParams)

    open fun post(path: String = "", body: RequestBody, headers: Map<String, String> = mapOf()) =
        fetch(method = "POST", path = path, body = body, headers = headers, queryParams = mapOf())

    open fun postJson(payload: String): Response {
        val requestBody = payload.toRequestBody("application/json".toMediaType())
        return post(body = requestBody)
    }

    open fun patch(path: String = "", body: RequestBody, headers: Map<String, String> = mapOf()) =
        fetch(method = "PATCH", path = path, body = body, headers = headers, queryParams = mapOf())

    open fun patchJson(path: String = "", payload: String): Response {
        val requestBody = payload.toRequestBody("application/json".toMediaType())
        return patch(path = path, body = requestBody)
    }

    open fun delete(path: String = "", body: RequestBody? = null, headers: Map<String, String> = mapOf()) =
        fetch(method = "DELETE", path = path, body = body, headers = headers, queryParams = mapOf())

    private fun fetch(
        method: String = "GET",
        path: String,
        body: RequestBody? = null,
        headers: Map<String, String>,
        queryParams: Map<String, List<String?>>
    ): Response {
        val request = Request.Builder().url(createUrl(path, queryParams))

        when (method) {
            "POST" -> request.post(body ?: EMPTY_REQUEST)
            "PATCH" -> request.patch(body ?: EMPTY_REQUEST)
            "DELETE" -> request.delete(body)
        }

        headers.forEach {
            request.addHeader(it.key, it.value)
        }

        return client.newCall(request.build()).execute()
    }

    private fun createUrl(path: String, params: Map<String, List<String?>>): HttpUrl {
        var url = stripSlashes(if (baseUrl.isNullOrBlank()) path else "$baseUrl${slash(path)}")

        if (params.isNotEmpty()) {
            url += (if (url.contains("?")) "&" else "?") + queryString(params)
        }
        return url.toHttpUrlOrNull() ?: throw MalformedURLException("Invalid URL: $url")
    }

    companion object {
        fun fetcher(timeout: Duration = REQUEST_TIMEOUT) = HttpFetcher(null, timeout)

        fun fetcher(baseUrl: String, timeout: Duration = REQUEST_TIMEOUT) = HttpFetcher(baseUrl, timeout)

        fun buildForm(form: Map<String, String>): FormBody {
            val builder = FormBody.Builder()
            form.forEach { (key, value) -> builder.add(key, value) }
            return builder.build()
        }

        fun Response.readBody(): String = this.use {
            if (it.isSuccessful) {
                it.body?.string() ?: throw EmptyResponseException(it)
            } else throw UnsuccessfulResponseException(it)
        }

        fun Response.useBody(block: (InputStream) -> Unit) = this.use {
            if (it.isSuccessful) {
                block(body?.byteStream() ?: throw EmptyResponseException(it))
            } else throw UnsuccessfulResponseException(it)
        }

        fun Response.toResponseEntity(): ResponseEntity<String> = this.use {
            ResponseEntity.status(it.code).body(it.body?.string())
        }

        private val CONNECTION_TIMEOUT = ofSeconds(5)
        private val REQUEST_TIMEOUT = ofSeconds(10)
        private val CONNECTIONPOOL_KEEPALIVE = ofMinutes(5)
        private const val MAX_TOTAL_CONNECTIONS = 20

        private fun slash(path: String) = if (path.isEmpty() || path[0] == '/') path else "/$path"
        private fun stripSlashes(str: String) = str.replace(Regex("(?<=[^:\\s])(/+/)"), "/")

        private fun queryString(queryParams: Map<String, List<String?>>) =
            queryParams.flatMap { (key, values) -> values.map { "$key=$it" } }.joinToString("&")
    }

    internal class EmptyResponseException(response: Response) :
        RuntimeException("Empty response (${response.code}) from ${response.request.url})")

    internal class UnsuccessfulResponseException(response: Response) :
        RuntimeException("Invalid response (${response.code}) from (${response.request.url}): ${response.body?.string()}")
}
