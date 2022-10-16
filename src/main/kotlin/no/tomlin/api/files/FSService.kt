package no.tomlin.api.files

import no.tomlin.api.config.ApiProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

@Service
class FSService(private val properties: ApiProperties) : FileService {

    override fun get(path: String?): List<FileModel> {
        val directory = getValidFile(path)

        checkExists(directory)
        checkDir(directory)

        return getFilesFromPath(directory).map { FileModel(it, properties.fileRoot) }
    }

    override fun mkdir(path: String): Boolean {
        val directory = getValidFile(path)

        checkNotExists(directory)

        return directory.mkdirs()
    }

    override fun rmdir(path: String): Boolean {
        val directory = getValidFile(path)

        checkExists(directory)
        checkDir(directory)

        return directory.deleteRecursively()
    }

    override fun remove(path: String): Boolean {
        val file = getValidFile(path)

        checkExists(file)
        checkFile(file)

        return file.delete()
    }

    override fun rename(old: String, new: String): Boolean {
        val oldFile = getValidFile(old)
        val newFile = File("${oldFile.parent}/$new")

        if (oldFile.path == newFile.path) {
            return true
        }

        checkExists(oldFile)
        checkNotExists(newFile)

        return oldFile.renameTo(newFile)
    }

    override fun download(path: String): File {
        val file = getValidFile(path)

        checkExists(file)
        checkFile(file)

        return file
    }

    override fun upload(path: String, files: Array<MultipartFile>): Int =
        files.count {
            val file = getValidFile("$path/${it.originalFilename}")

            checkNotExists(file)

            Files.copy(it.inputStream, file.toPath(), REPLACE_EXISTING)
            true
        }

    override fun store(path: String, data: InputStream, contentType: String?): Boolean {
        File(path).writeBytes(data.readAllBytes())
        return true
    }

    private fun getValidFile(path: String?) = File(properties.fileRoot + prependSlash(stripRelative(path.orEmpty())))

    private companion object {
        fun prependSlash(path: String): String = if (path.isEmpty() || path[0] == '/') path else "/$path"

        fun stripRelative(path: String): String = path.split('/').filter { it != "." && it != ".." }.joinToString("/")

        fun getFilesFromPath(path: File): List<File> = path.listFiles().orEmpty().toList()

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