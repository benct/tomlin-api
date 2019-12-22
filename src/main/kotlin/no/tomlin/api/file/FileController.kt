package no.tomlin.api.file

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.io.File
import java.lang.Math.pow

@RestController
@RequestMapping("/file")
class FileController {

    @Secured(USER, ADMIN)
    @GetMapping("/tree")
    fun getFiles(@RequestParam path: String?): List<FileModel> {
        val file = File("$ROOT${slash(path)}")

        validatePath(file)

        return getFilesFromPath(file).map {
            FileModel(
                it.path,
                it.name,
                shortName(it.name),
                computeSize(it.length()),
                it.extension,
                it.isDirectory,
                canPreview(it.extension),
                it.listFiles()?.size ?: 0
            )
        }
    }

    private fun validatePath(file: File, checkIsDir: Boolean = true): Boolean {
        if (file.path.split('/').contains("..")) {
            throw IllegalArgumentException("Parent directory references are not permitted")
        }
        if (file.path.split('/').contains(".")) {
            throw IllegalArgumentException("Relative directory references are not permitted")
        }
        if (!file.exists()) {
            throw IllegalArgumentException("The specified path does not exist")
        }
        if (checkIsDir && !file.isDirectory) {
            throw IllegalArgumentException("The specified path is not a valid directory")
        }
        if (!checkIsDir && !file.isFile) {
            throw IllegalArgumentException("The specified path is not a valid file")
        }
        return true
    }

    companion object {
        private const val ROOT = "files"
        private const val KILO: Double = 1024.0
        private const val PREVIEW = "jpg|jpeg|png|bmp|gif|svg|ico|txt|md"

        private fun getFilesFromPath(path: File): List<File> = path.listFiles().orEmpty().toList()

        private fun computeSize(sizeInBytes: Long): String {
            val total = sizeInBytes.toDouble()

            return when {
                total < KILO -> "$total  b"
                total < pow(KILO, 2.0) -> "${total / KILO} kb"
                total < pow(KILO, 3.0) -> "${total / (KILO * KILO)} mb"
                else -> "${total / (KILO * KILO * KILO)} gb"
            }
        }

        private fun shortName(name: String): String = if (name.length > 25) "${name.substring(0, 21)}..${name.substring(-4)}" else name

        private fun canPreview(extension: String) = extension.isNotBlank() && PREVIEW.contains(extension)

        private fun slash(path: String?): String = path?.let { if (it.isEmpty() || path[0] == '/') path else "/$path" } ?: ""
    }

    data class FileModel(
        val path: String,
        val name: String,
        val short: String,
        val size: String,
        val type: String,
        val dir: Boolean,
        val preview: Boolean,
        val files: Int = 0
    )
}
