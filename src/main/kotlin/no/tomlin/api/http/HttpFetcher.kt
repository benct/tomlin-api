package no.tomlin.api.http

import okhttp3.*
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

    open fun getJson(path: String, queryParams: Map<String, List<String>> = mapOf()) =
        get(path, mapOf("Accept" to "application/json"), queryParams)

    open fun getJson(queryParams: Map<String, String> = mapOf()) =
        get("", mapOf("Accept" to "application/json"), queryParams.mapValues { listOf(it.value) })

    open fun get(path: String = "", headers: Map<String, String> = mapOf(), queryParams: Map<String, List<String>> = mapOf()) =
        fetch(path = path, headers = headers, queryParams = queryParams)

    open fun get(headers: Map<String, String>, queryParams: Map<String, String?>) =
        fetch(path = "", headers = headers, queryParams = queryParams.mapValues { listOf(it.value) })

    open fun post(path: String, postBody: RequestBody, headers: Map<String, String> = mapOf()) =
        fetch(path, postBody, headers, mapOf())

    open fun post(postBody: RequestBody, headers: Map<String, String> = mapOf()) = fetch("", postBody, headers, mapOf())

    open fun postJson(payload: String): Response {
        val requestBody = RequestBody.create(MediaType.parse("application/json"), payload)
        return post(requestBody)
    }

    private fun fetch(
        path: String,
        postBody: RequestBody? = null,
        headers: Map<String, String>,
        queryParams: Map<String, List<String?>>
    ): Response {

        val request = Request.Builder().url(createUrl(path, queryParams))
        postBody?.let {
            request.post(it)
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
        HttpUrl.parse(url)?.let {
            return it
        }
        throw MalformedURLException("Invalid URL: $url")
    }

    companion object {
        fun fetcher(timeout: Duration = REQUEST_TIMEOUT) = HttpFetcher(null, timeout)

        fun fetcher(baseUrl: String, timeout: Duration = REQUEST_TIMEOUT) = HttpFetcher(baseUrl, timeout)

        fun buildForm(form: Map<String, String>): FormBody {
            val builder = FormBody.Builder()
            form.forEach { (key, value) -> builder.add(key, value) }
            return builder.build()
        }

        private val CONNECTION_TIMEOUT = ofSeconds(5)
        private val REQUEST_TIMEOUT = ofSeconds(10)
        private val CONNECTIONPOOL_KEEPALIVE = ofMinutes(5)
        private const val MAX_TOTAL_CONNECTIONS = 120

        private fun slash(path: String) = if (path.isEmpty() || path[0] == '/') path else "/$path"
        private fun stripSlashes(str: String) = str.replace(Regex("(?<=[^:\\s])(/+/)"), "/")

        private fun queryString(queryParams: Map<String, List<String?>>) =
            queryParams.flatMap { (key, values) -> values.map { "$key=$it" } }.joinToString("&")
    }
}