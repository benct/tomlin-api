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

    var key: String = UUID.randomUUID().toString(),

    var cdn: CdnProperties = CdnProperties(),
    var files: FileProperties = FileProperties(),
    var backup: BackupProperties = BackupProperties(),
    var tmdb: TmdbProperties = TmdbProperties()
) {
    data class CdnProperties(
        var images: String = "$CDN_ROOT/images",
        var poster: String = "$CDN_ROOT/images/media",
        var icons: String = "$CDN_ROOT/icons",
        var file: String = "$CDN_ROOT/icons/file"
    )

    data class FileProperties(
        var path: String = "/var/api/files"
    )

    data class BackupProperties(
        var path: String = "/var/api/backup",
        var params: String = "-u user -ppassword database"
    )

    data class TmdbProperties(
        var key: String = UUID.randomUUID().toString(),
        var url: String = "https://api.themoviedb.org/3/",
        var posterUrl: String = "https://image.tmdb.org/t/p/w200"
    )

    private companion object {
        const val CDN_ROOT = "/var/www/html"
    }
}
