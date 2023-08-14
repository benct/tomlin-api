package no.tomlin.api

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import no.tomlin.api.admin.dao.AdminDao
import no.tomlin.api.admin.dao.UserDao
import no.tomlin.api.admin.service.GCPService
import no.tomlin.api.config.ApiProperties
import no.tomlin.api.github.GitHubService
import no.tomlin.api.weather.WeatherService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.awt.image.BufferedImage
import java.security.Principal
import java.time.LocalDate.now
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class ApiController(
    private val properties: ApiProperties,
    private val adminDao: AdminDao,
    private val userDao: UserDao,
    private val gitHubService: GitHubService,
    private val weatherService: WeatherService,
    private val gcpService: GCPService,
) {

    @GetMapping("/", produces = [APPLICATION_JSON_VALUE])
    fun base() = mapOf("site" to properties.name, "version" to properties.version, "baseUrl" to properties.baseUrl)

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("/version")
    fun version() = properties.version

    @GetMapping("/github", produces = [APPLICATION_JSON_VALUE])
    fun github() = gitHubService.getUserData()

    @GetMapping("/weather", produces = [APPLICATION_JSON_VALUE])
    fun weather() = weatherService.getWeather()

    @PostMapping("/authenticate", produces = [APPLICATION_JSON_VALUE])
    fun authenticate(
        @RequestParam referrer: String?,
        request: HttpServletRequest,
        principal: Principal?
    ): AuthResponse =
        try {
            adminDao.visit(
                request.remoteAddr,
                request.remoteHost,
                referrer,
                request.getHeader("User-Agent"),
                request.getHeader("referer")
            )

            principal?.let {
                userDao.updateLastSeen(it.name)
            }

            AuthResponse(principal, weatherService.getWeather(), adminDao.getSettings())
        } catch (_: Exception) {
            // Database is probably down, return state that is not data dependent...
            AuthResponse(principal, weatherService.getWeather())
        }

    @PostMapping("/login")
    fun login(request: HttpServletRequest, response: HttpServletResponse) = request.authenticate(response)

    @PostMapping("/database/{action}", produces = [APPLICATION_JSON_VALUE])
    fun gcpDatabase(@PathVariable action: String) = gcpService.handleDatabaseAction(action)

    @GetMapping("/qr", produces = [IMAGE_PNG_VALUE])
    fun qrCode(@RequestParam content: String?): BufferedImage? {
        val bitMatrix = QRCodeWriter().encode(content ?: properties.baseUrl, BarcodeFormat.QR_CODE, 256, 256)
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }

    data class AuthResponse(
        val authenticated: Boolean,
        val database: Boolean,
        val settings: Map<String, Any?>,
        val weather: Map<String, Any?>,
        val username: String?,
    ) {
        constructor(principal: Principal?, weather: Map<String, Any?>, settings: Map<String, Any?>? = null) : this(
            authenticated = SecurityContextHolder.getContext().authentication?.isAuthenticated ?: false,
            database = settings != null,
            settings = settings ?: mapOf("countdownTarget" to now().plusDays(15).atStartOfDay()),
            weather = weather,
            username = principal?.name,
        )
    }
}
