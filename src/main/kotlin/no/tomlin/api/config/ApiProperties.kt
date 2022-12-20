package no.tomlin.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConfigurationProperties("api")
data class ApiProperties(
    var name: String = "Tomlin API",
    var version: String = "1.0.0",
    var baseUrl: String = "https://tomlin.no",

    // Values below should/will be overwritten by application.yaml properties:

    var key: String = UUID.randomUUID().toString(),
    var tmdbKey: String = UUID.randomUUID().toString(),

    var dbInstance: String = "some-db-instance",

    var cdnRoot: String = "/var/www/html",
    var fileRoot: String = "/var/api/files",

    var backup: String = "-u user -ppassword database",
)
