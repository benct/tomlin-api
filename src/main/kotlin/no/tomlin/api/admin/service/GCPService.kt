package no.tomlin.api.admin.service

import no.tomlin.api.config.ApiProperties
import no.tomlin.api.http.HttpFetcher
import no.tomlin.api.http.HttpFetcher.Companion.fetcher
import no.tomlin.api.http.HttpFetcher.Companion.toResponseEntity
import okhttp3.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class GCPService(val fetcher: HttpFetcher = fetcher(GCP_SQL_URL)) {

    @Autowired
    private lateinit var properties: ApiProperties

    fun handleDatabaseStartStop(action: String): ResponseEntity<String> {
        val response = when (action) {
            "start" -> runSqlAction(ActivationPolicy.ALWAYS)
            "stop" -> runSqlAction(ActivationPolicy.NEVER)
            "restart" -> runSqlAction(ActivationPolicy.ALWAYS, restart = true)
            else -> throw IllegalArgumentException("Invalid action '$action' provided.")
        }

        return response.toResponseEntity()
    }

    private fun runSqlAction(policy: ActivationPolicy, restart: Boolean = false): Response =
        fetcher.patchJson("/instances/${properties.dbInstance}${if (restart) "/restart" else ""}", body(policy))

    private companion object {
        const val PROJECT_ID = "tomlin-server"
        const val GCP_SQL_URL = "https://sqladmin.googleapis.com/v1/projects/$PROJECT_ID"

        enum class ActivationPolicy {
            ALWAYS,
            NEVER,
        }

        fun body(policy: ActivationPolicy) = """{"settings":{"activationPolicy":"${policy.name}"}}"""
    }
}