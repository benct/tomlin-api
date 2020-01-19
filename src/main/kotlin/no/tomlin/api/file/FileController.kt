package no.tomlin.api.file

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import no.tomlin.api.config.ApiProperties
import no.tomlin.api.file.entity.FileModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/file")
class FileController {

    @Autowired
    private lateinit var properties: ApiProperties

    private var extensionIcons: List<String> = emptyList()

    @PostConstruct
    private fun init() {
        val directory = File(properties.cdn.file).also { it.mkdirs() }

        extensionIcons = getFilesFromPath(directory).map { it.nameWithoutExtension }
    }

    @Secured(USER, ADMIN)
    @GetMapping("/tree")
    fun getFiles(@RequestParam path: String?): List<FileModel> {
        val directory = getValidFile(path)

        checkExists(directory)
        checkDir(directory)

        return getFilesFromPath(directory)
            .map { FileModel(it, properties.files.path, extensionIcons.contains(it.extension)) }
            .sortedBy(::sortByDirAndName)
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
    fun remove(@RequestParam path: String): Boolean {
        val file = getValidFile(path)

        checkExists(file)
        checkFile(file)

        return file.delete()
    }

    @Secured(ADMIN)
    @PostMapping("/rename")
    fun rename(@RequestParam old: String, @RequestParam new: String): Boolean {
        val oldFile = getValidFile(old)
        val newFile = File("${oldFile.parent}/$new")

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

    private fun getValidFile(path: String?) = File(properties.files.path + prependSlash(stripRelative(path.orEmpty())))

    private companion object {
        fun prependSlash(path: String): String = if (path.isEmpty() || path[0] == '/') path else "/$path"

        fun stripRelative(path: String): String = path.split('/').filter { it != "." && it != ".." }.joinToString("/")

        fun getFilesFromPath(path: File): List<File> = path.listFiles().orEmpty().toList()

        fun sortByDirAndName(file: FileModel): String = (if (file.dir) "00" else "0") + file.name

        fun checkExists(file: File) {
            if (!file.exists()) {
                throw IllegalStateException("The path '${file.path}' does not exist")
            }
        }

        fun checkNotExists(file: File) {
            if (file.exists()) {
                throw IllegalStateException("The file or directory '${file.path}' already exists")
            }
        }

        fun checkDir(file: File) {
            if (!file.isDirectory) {
                throw IllegalStateException("The path '${file.path}' is not a valid directory")
            }
        }

        fun checkFile(file: File) {
            if (!file.isFile) {
                throw IllegalStateException("The path '${file.path}' is not a valid file")
            }
        }
    }
}
