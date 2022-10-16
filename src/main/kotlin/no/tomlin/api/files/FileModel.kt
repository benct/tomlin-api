package no.tomlin.api.files

import com.google.cloud.storage.Blob
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.pow

data class FileModel(
    val path: String,
    val name: String,
    val short: String,
    val size: String,
    val modified: String?,
    val ext: String,
    val isDir: Boolean,
    val preview: String?,
    val contentType: String? = null,
    val perms: String? = null,
    val files: Int? = null,
) {
    constructor(file: File, root: String) : this(
        file.path.replace(root, ""),
        file.name,
        shortName(file.name),
        computeSize(file.length()),
        computeModified(file.lastModified()),
        if (file.isDirectory) "dir" else file.extension.lowercase(),
        file.isDirectory,
        findPreviewType(file.extension.lowercase()),
        null,
        computePerms(file),
        file.listFiles()?.size ?: 0
    )

    constructor(file: Blob) : this(
        file.name,
        file.name.trim('/').split('/').last(),
        shortName(file.name.trim('/').split('/').last()),
        computeSize(file.size),
        file.updateTime?.let { computeModified(it) },
        if (file.isDirectory) "dir" else file.name.split('.').last().lowercase(),
        file.isDirectory,
        findPreviewType(file.name.split('.').last().lowercase()),
        file.contentType,
    )

    private companion object {
        const val KILO: Double = 1024.0

        val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/YY")
        val NUM_FORMAT = DecimalFormat("#,###.##")

        fun shortName(name: String): String = if (name.length > 25) "${name.take(15)}...${name.takeLast(7)}" else name

        fun computeSize(sizeInBytes: Long): String {
            val total = sizeInBytes.toDouble()

            return when {
                total < KILO -> "$sizeInBytes  b"
                total < KILO.pow(2.0) -> "${NUM_FORMAT.format(total / KILO)} kb"
                total < KILO.pow(3.0) -> "${NUM_FORMAT.format(total / (KILO * KILO))} mb"
                else -> "${NUM_FORMAT.format(total / (KILO * KILO * KILO))} gb"
            }
        }

        fun computeModified(modified: Long): String =
            LocalDateTime
                .ofInstant(Instant.ofEpochMilli(modified), ZoneId.systemDefault())
                .format(DATE_FORMAT)


        fun computePerms(file: File): String =
            PosixFilePermissions.toString(Files.getPosixFilePermissions(file.toPath()))

        fun findPreviewType(ext: String?, contentType: String? = null): String? =
            Preview.values()
                .find { it.extensions.contains(ext?.lowercase()) || contentType?.contains(it.name.lowercase()) == true }
                ?.name
                ?.lowercase()

        enum class Preview(val extensions: List<String>) {
            IMAGE(listOf("jpg", "jpeg", "png", "bmp", "gif", "svg", "ico")),
            VIDEO(listOf("avi", "mp4", "webm", "ogm", "ogv", "ogg")),
            TEXT(listOf("txt", "json", "xml", "ini", "conf", "html", "js", "css", "md"))
        }
    }
}
