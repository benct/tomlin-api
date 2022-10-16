package no.tomlin.api.files

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream

interface FileService {

    fun get(path: String?): List<FileModel>

    fun mkdir(path: String): Boolean

    fun rmdir(path: String): Boolean

    fun remove(path: String): Boolean

    fun rename(old: String, new: String): Boolean

    fun download(path: String): File

    fun upload(path: String, files: Array<MultipartFile>): Int

    fun store(path: String, data: InputStream, contentType: String? = null): Boolean
}