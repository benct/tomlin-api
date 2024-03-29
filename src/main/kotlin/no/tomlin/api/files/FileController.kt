package no.tomlin.api.files

import jakarta.servlet.http.HttpServletResponse
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.PRIVATE
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/file")
class FileController(@Qualifier("GCSService") private val fileService: FileService) {

    @Secured(ADMIN, PRIVATE)
    @GetMapping("/tree")
    fun getFiles(@RequestParam path: String?): List<FileModel> =
        fileService.get(path).sortedBy(Companion::sortByDirAndName)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/mkdir")
    fun mkdir(@RequestParam path: String): Boolean = fileService.mkdir(path)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/rmdir")
    fun rmdir(@RequestParam path: String): Boolean = fileService.rmdir(path)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/remove")
    fun remove(@RequestParam path: String): Boolean = fileService.remove(path)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/rename")
    fun rename(@RequestParam old: String, @RequestParam new: String): Boolean = fileService.rename(old, new)

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/download")
    fun download(@RequestParam path: String, @RequestHeader referer: String?, response: HttpServletResponse) {
        if (referer.isNullOrEmpty()) {
            // TODO check actual referer string?
            throw IllegalStateException("No cross-referencing allowed")
        }

        val file = fileService.download(path)

        response.contentType = "application/save"
        response.setContentLengthLong(file.length())
        response.addHeader("Content-Disposition", "attachment; filename=${file.name}")
        response.addHeader("Content-Transfer-Encoding", "binary")

        Files.copy(Paths.get(file.path), response.outputStream)
        response.outputStream.flush()
    }

    @Secured(ADMIN, PRIVATE)
    @PostMapping("/upload")
    fun upload(@RequestParam path: String, @RequestParam files: Array<MultipartFile>): Int =
        fileService.upload(path, files)

    private companion object {
        fun sortByDirAndName(file: FileModel): String = (if (file.isDir) "00" else "0") + file.name
    }
}
