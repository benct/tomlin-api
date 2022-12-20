package no.tomlin.api.admin.service

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sqladmin.SQLAdmin
import com.google.api.services.sqladmin.model.DatabaseInstance
import com.google.api.services.sqladmin.model.Operation
import com.google.api.services.sqladmin.model.Settings
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

    fun handleDatabaseStartStop(action: String): Operation {
        val response = when (action) {
            "start" -> updateGCPDatabase(ActivationPolicy.ALWAYS)
            "stop" -> updateGCPDatabase(ActivationPolicy.NEVER)
            "restart" -> updateGCPDatabase(ActivationPolicy.ALWAYS, restart = true)
            else -> throw IllegalArgumentException("Invalid action '$action' provided.")
        }
        return response
    }

    private fun updateGCPDatabase(policy: ActivationPolicy, restart: Boolean = false): Operation {
        val sqlAdminService: SQLAdmin = createSqlAdminService()
        val request = if (restart) {
            sqlAdminService.instances().restart(PROJECT_ID, properties.dbInstance)
        } else {
            val requestBody = DatabaseInstance().apply {
                settings = Settings().apply { activationPolicy = policy.name }
            }
            sqlAdminService.instances().patch(PROJECT_ID, properties.dbInstance, requestBody)
        }

        val response: Operation = request.execute()

        // TODO: Change code below to process the `response` object:
        println(response)

        return response
    }

    fun _handleDatabaseStartStop(action: String): ResponseEntity<String> {
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

        fun createSqlAdminService(): SQLAdmin {
            val httpTransport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
            var credential: GoogleCredential = GoogleCredential.getApplicationDefault()
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
            }
            return SQLAdmin.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Tomlin-API/1.0")
                .build()
        }
    }
}