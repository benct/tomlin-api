package no.tomlin.api.file

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.servlet.http.HttpServletResponse
import kotlin.math.pow

@RestController
@RequestMapping("/file")
class FileController {

    @Secured(USER, ADMIN)
    @GetMapping("/tree")
    fun getFiles(@RequestParam path: String?): List<FileModel> {
        val file = getValidFile(path)

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

    @Secured(ADMIN)
    @PostMapping("/mkdir")
    fun mkdir(@RequestParam path: String): Boolean {
        val file = getValidFile(path)

        if (file.exists()) {
            throw IllegalStateException("A directory with the name '${file.name}' already exists")
        }
        return file.mkdirs()
    }

    @Secured(ADMIN)
    @PostMapping("/rmdir")
    fun rmdir(@RequestParam path: String): Boolean {
        val file = getValidFile(path)

        if (!file.exists()) {
            throw IllegalStateException("The specified directory path does not exists")
        }
        return file.deleteRecursively()
    }

    @Secured(ADMIN)
    @PostMapping("/download")
    fun download(@RequestParam path: String, @RequestHeader referer: String?, response: HttpServletResponse) {
        val file = getValidFile(path)

        if (referer.isNullOrEmpty()) {
            // TODO check actual referer string?
            throw IllegalArgumentException("No cross-referencing allowed")
        }

        validatePath(file, false)

        response.contentType = "application/save"
        response.setContentLengthLong(file.length())
        response.addHeader("Content-Disposition", "attachment; filename=${file.name}")
        response.addHeader("Content-Transfer-Encoding", "binary")

        Files.copy(Paths.get(file.path), response.outputStream)
        response.outputStream.flush()
    }

    private fun validatePath(file: File, isDir: Boolean = true): Boolean {
        if (!file.exists()) {
            throw IllegalArgumentException("The specified path does not exist")
        }
        if (isDir && !file.isDirectory) {
            throw IllegalArgumentException("The specified path is not a valid directory")
        }
        if (!isDir && !file.isFile) {
            throw IllegalArgumentException("The specified path is not a valid file")
        }
        return true
    }

    companion object {
        private const val ROOT = "files"
        private const val KILO: Double = 1024.0
        private const val PREVIEW = "jpg|jpeg|png|bmp|gif|svg|ico|txt|md"

        private fun getValidFile(path: String?) = File(ROOT + prependSlash(stripRelative(path.orEmpty())))

        private fun getFilesFromPath(path: File): List<File> = path.listFiles().orEmpty().toList()

        private fun computeSize(sizeInBytes: Long): String {
            val total = sizeInBytes.toDouble()

            return when {
                total < KILO -> "$total  b"
                total < KILO.pow(2.0) -> "${total / KILO} kb"
                total < KILO.pow(3.0) -> "${total / (KILO * KILO)} mb"
                else -> "${total / (KILO * KILO * KILO)} gb"
            }
        }

        private fun shortName(name: String): String = if (name.length > 25) "${name.substring(0, 21)}..${name.substring(-4)}" else name

        private fun canPreview(extension: String) = extension.isNotBlank() && PREVIEW.contains(extension)

        private fun prependSlash(path: String): String = if (path.isEmpty() || path[0] == '/') path else "/$path"

        private fun stripRelative(path: String): String = path.split('/').filter { it != "." && it != ".." }.joinToString("/")
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
