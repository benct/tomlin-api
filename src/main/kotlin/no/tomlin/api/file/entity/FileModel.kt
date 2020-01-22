package no.tomlin.api.file.entity

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
    val perms: String,
    val date: String,
    val type: String,
    val dir: Boolean,
    val icon: Boolean,
    val preview: Boolean,
    val files: Int = 0
) {
    constructor(file: File, root: String, hasIcon: Boolean) : this(
        file.path.replace(root, ""),
        file.name,
        shortName(file.name),
        computeSize(file.length()),
        computePerms(file),
        computeModified(file),
        if (file.isDirectory) "dir" else file.extension,
        file.isDirectory,
        hasIcon || file.isDirectory,
        canPreview(file.extension),
        file.listFiles()?.size ?: 0
    )

    private companion object {
        const val KILO: Double = 1024.0
        const val PREVIEW = "jpg|jpeg|png|bmp|gif|svg|ico|txt|md"

        val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/YY")
        val NUM_FORMAT = DecimalFormat("#,###.##")

        fun shortName(name: String): String = if (name.length > 25) "${name.take(21)}..${name.takeLast(4)}" else name

        fun canPreview(extension: String) = extension.isNotBlank() && PREVIEW.contains(extension)

        fun computeSize(sizeInBytes: Long): String {
            val total = sizeInBytes.toDouble()

            return when {
                total < KILO -> "$sizeInBytes  b"
                total < KILO.pow(2.0) -> "${NUM_FORMAT.format(total / KILO)} kb"
                total < KILO.pow(3.0) -> "${NUM_FORMAT.format(total / (KILO * KILO))} mb"
                else -> "${NUM_FORMAT.format(total / (KILO * KILO * KILO))} gb"
            }
        }

        fun computePerms(file: File): String = PosixFilePermissions.toString(Files.getPosixFilePermissions(file.toPath()))

        fun computeModified(file: File): String =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()).format(DATE_FORMAT)
    }
}
