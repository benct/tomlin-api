package no.tomlin.api.files

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.config.ApiProperties
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.nio.file.Paths


@Service
class GCSService(private val properties: ApiProperties) : FileService {

    init {
        File(TEMP_FILES).mkdirs()
    }

    override fun get(path: String?): List<FileModel> =
        blobs(path).values
            .filter { it.isDirectory || !it.name.endsWith("/") } // remove references to self
            .map { FileModel(it) }

    override fun mkdir(path: String): Boolean =
        storage.create(BlobInfo.newBuilder(blobId(path, "/")).build(), Storage.BlobTargetOption.doesNotExist()) != null

    override fun rmdir(path: String): Boolean {
        blobs(path).values.forEach {
            it.delete()
        }
        return storage.delete(BUCKET_NAME, getValidPath(path, "/"))
    }

    override fun remove(path: String): Boolean = storage.delete(BUCKET_NAME, getValidPath(path))

    override fun rename(old: String, new: String): Boolean {
        val source = blobId(old)

        val parentLevel = new.split('/').count { it.trim() == ".." } + 1
        val parentPath = getValidPath(old).split('/').dropLast(parentLevel).joinToString("/")
        val target = blobId(parentPath.nullIfBlank()?.plus("/$new") ?: new)

        val precondition = Storage.BlobTargetOption.doesNotExist()
        storage.copy(Storage.CopyRequest.newBuilder().setSource(source).setTarget(target, precondition).build())

        val copiedObject = storage.get(target)
        storage.get(source).delete()

        return copiedObject != null
    }

    override fun download(path: String): File {
        val fileName = getValidPath(path).split('/').last()
        val blob = storage.get(blobId(path))
        blob.downloadTo(Paths.get(TEMP_FILES + fileName))
        return File(TEMP_FILES + fileName)
    }

    override fun upload(path: String, files: Array<MultipartFile>): Int =
        files.count {
            store("$path/${it.originalFilename}", it.inputStream, it.contentType)
        }

    override fun store(path: String, data: InputStream, contentType: String?): Boolean {
        val blobInfo: BlobInfo = BlobInfo.newBuilder(blobId(path)).setContentType(contentType).build()
        return storage.createFrom(blobInfo, data) != null
    }

    private fun blobs(path: String?) = storage.list(
        BUCKET_NAME,
        Storage.BlobListOption.prefix(getValidPath(path, "/")),
        Storage.BlobListOption.currentDirectory()
    )

    private fun blobId(path: String, prepend: String = "") = BlobId.of(BUCKET_NAME, getValidPath(path, prepend))

    // Valid file or directory path without leading or (optional) trailing slash
    private fun getValidPath(path: String?, prepend: String = "") =
        if (path.isNullOrBlank()) ""
        else "${properties.fileRoot}/${path}"
            .split("/")
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "." && it != ".." }
            .joinToString("/") + prepend

    private companion object {
        const val PROJECT_ID = "tomlin-server"
        const val BUCKET_NAME = "tomlin-cdn"
        const val TEMP_FILES = "/tmp/api/"

        val storage: Storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().service
    }
}