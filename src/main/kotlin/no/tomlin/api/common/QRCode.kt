package no.tomlin.api.common

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

object QRCode {

    private val qrCodeWriter = QRCodeWriter()

    fun generateQRCodeImage(content: String, size: Int = 150): BufferedImage {
        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }

    fun generateQRCodeStream(content: String, size: Int = 150): InputStream {
        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }
}