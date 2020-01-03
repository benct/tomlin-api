package no.tomlin.api.file

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.file.entity.FileModel
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import javax.servlet.http.HttpServletResponse
import kotlin.math.pow

@RestController
@RequestMapping("/file")
class FileController {

    @Secured(USER, ADMIN)
    @GetMapping("/tree")
    fun getFiles(@RequestParam path: String?): List<FileModel> {
        val directory = getValidFile(path)

        checkExists(directory)
        checkDir(directory)

        return getFilesFromPath(directory).map {
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
        val directory = getValidFile(path)

        checkNotExists(directory)

        return directory.mkdirs()
    }

    @Secured(ADMIN)
    @PostMapping("/rmdir")
    fun rmdir(@RequestParam path: String): Boolean {
        val directory = getValidFile(path)

        checkExists(directory)
        checkDir(directory)

        return directory.deleteRecursively()
    }

    @Secured(ADMIN)
    @PostMapping("/remove")
    fun rm(@RequestParam path: String): Boolean {
        val file = getValidFile(path)

        checkExists(file)
        checkFile(file)

        return file.delete()
    }

    @Secured(ADMIN)
    @PostMapping("/rename")
    fun rename(@RequestParam old: String, @RequestParam new: String): Boolean {
        val oldFile = getValidFile(old)
        val newFile = getValidFile(new)

        if (oldFile.path == newFile.path) {
            return true
        }

        checkExists(oldFile)
        checkNotExists(newFile)

        return oldFile.renameTo(newFile)
    }

    @Secured(ADMIN)
    @PostMapping("/download")
    fun download(@RequestParam path: String, @RequestHeader referer: String?, response: HttpServletResponse) {
        val file = getValidFile(path)

        if (referer.isNullOrEmpty()) {
            // TODO check actual referer string?
            throw IllegalStateException("No cross-referencing allowed")
        }

        checkExists(file)
        checkFile(file)

        response.contentType = "application/save"
        response.setContentLengthLong(file.length())
        response.addHeader("Content-Disposition", "attachment; filename=${file.name}")
        response.addHeader("Content-Transfer-Encoding", "binary")

        Files.copy(Paths.get(file.path), response.outputStream)
        response.outputStream.flush()
    }

    @Secured(USER, ADMIN)
    @PostMapping("/upload")
    fun upload(@RequestParam path: String, @RequestParam files: Array<MultipartFile>): Int =
        files.count {
            val file = getValidFile("$path/${it.originalFilename}")

            checkNotExists(file)

            Files.copy(it.inputStream, file.toPath(), REPLACE_EXISTING)
            true
        }

    companion object {
        private const val ROOT = "files"
        private const val KILO: Double = 1024.0
        private const val PREVIEW = "jpg|jpeg|png|bmp|gif|svg|ico|txt|md"

        private fun getValidFile(path: String?) = File(ROOT + prependSlash(stripRelative(path.orEmpty())))

        private fun prependSlash(path: String): String = if (path.isEmpty() || path[0] == '/') path else "/$path"

        private fun stripRelative(path: String): String = path.split('/').filter { it != "." && it != ".." }.joinToString("/")

        private fun getFilesFromPath(path: File): List<File> = path.listFiles().orEmpty().toList()

        private fun shortName(name: String): String = if (name.length > 25) "${name.substring(0, 21)}..${name.substring(-4)}" else name

        private fun canPreview(extension: String) = extension.isNotBlank() && PREVIEW.contains(extension)

        private fun computeSize(sizeInBytes: Long): String {
            val total = sizeInBytes.toDouble()

            return when {
                total < KILO -> "$total  b"
                total < KILO.pow(2.0) -> "${total / KILO} kb"
                total < KILO.pow(3.0) -> "${total / (KILO * KILO)} mb"
                else -> "${total / (KILO * KILO * KILO)} gb"
            }
        }

        private fun checkExists(file: File) {
            if (!file.exists()) {
                throw IllegalStateException("The specified path does not exist")
            }
        }

        private fun checkNotExists(file: File) {
            if (file.exists()) {
                throw IllegalStateException("The specified file or directory already exists")
            }
        }

        private fun checkDir(file: File) {
            if (!file.isDirectory) {
                throw IllegalStateException("The specified path is not a valid directory")
            }
        }

        private fun checkFile(file: File) {
            if (!file.isFile) {
                throw IllegalStateException("The specified path is not a valid file")
            }
        }
    }
}
