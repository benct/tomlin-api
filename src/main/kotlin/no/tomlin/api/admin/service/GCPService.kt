package no.tomlin.api.admin.service

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sqladmin.SQLAdmin
import com.google.api.services.sqladmin.model.DatabaseInstance
import com.google.api.services.sqladmin.model.IpConfiguration
import com.google.api.services.sqladmin.model.Operation
import com.google.api.services.sqladmin.model.Settings
import no.tomlin.api.config.ApiProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GCPService {

    @Autowired
    private lateinit var properties: ApiProperties

    fun handleDatabaseAction(action: String): Operation {
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
                settings = Settings().apply {
                    activationPolicy = policy.name
                    ipConfiguration = IpConfiguration().apply {
                        ipv4Enabled = (policy == ActivationPolicy.ALWAYS)
                    }
                }
            }
            sqlAdminService.instances().patch(PROJECT_ID, properties.dbInstance, requestBody)
        }
        return request.execute()
    }

    private companion object {
        const val PROJECT_ID = "tomlin-server"

        enum class ActivationPolicy {
            ALWAYS,
            NEVER,
        }

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