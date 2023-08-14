package no.tomlin.api.qrator

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.QRATOR
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.files.FileService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@RestController
@RequestMapping("/qrator")
class QratorController(
    private val qratorDao: QratorDao,
    @Qualifier("GCSService") private val fileService: FileService
) {

    @GetMapping
    fun all(): List<Map<String, Any?>> = qratorDao.get()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Map<String, Any?> = qratorDao.get(id)

    @Secured(ADMIN, QRATOR)
    @PostMapping("/upload")
    fun upload(@RequestParam files: Array<MultipartFile>): Int =
        files.count {
            val fileExtension = getExtension(it.originalFilename)
            qratorDao.create(fileExtension)?.let { id ->
                val stored = fileService.store("$ART_PATH/$id.${fileExtension}", it.inputStream, it.contentType)
                if (stored) {
                    val qrCodeImage = generateQRCode("$QR_BASE_URL/$id")
                    fileService.store("$QR_PATH/$id.png", qrCodeImage, IMAGE_PNG_VALUE)
                } else {
                    qratorDao.delete(id)
                }
            } ?: false
        }

    @Secured(ADMIN, QRATOR)
    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam title: String?,
        @RequestParam author: String?,
        @RequestParam value: Int?,
        @RequestParam description: String?,
    ): Boolean = qratorDao.update(id, title.nullIfBlank(), author.nullIfBlank(), value, description.nullIfBlank())

    @Secured(ADMIN, QRATOR)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Boolean = qratorDao.delete(id)

    private companion object {
        const val QR_BASE_URL = "https://tomlin.no/qrator"
        const val ART_PATH = "/qrator/art"
        const val QR_PATH = "/qrator/qr"

        fun getExtension(filename: String?): String? =
            filename?.takeIf { it.contains(".") }?.let { it.substring(it.lastIndexOf(".") + 1) }

        fun generateQRCode(content: String): InputStream {
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 150, 150)
            val outputStream = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
            return ByteArrayInputStream(outputStream.toByteArray())
        }
    }
}