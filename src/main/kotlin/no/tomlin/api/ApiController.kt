package no.tomlin.api

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.tomlin.api.admin.dao.AdminDao
import no.tomlin.api.admin.dao.UserDao
import no.tomlin.api.admin.service.GCPService
import no.tomlin.api.common.AuthUtils.getUserRoles
import no.tomlin.api.common.AuthUtils.isLoggedIn
import no.tomlin.api.common.JsonUtils.toJson
import no.tomlin.api.common.QRCode.generateQRCodeImage
import no.tomlin.api.config.ApiProperties
import no.tomlin.api.github.GitHubService
import no.tomlin.api.logging.LogDao
import no.tomlin.api.weather.WeatherService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDate.now

@RestController
class ApiController(
    private val properties: ApiProperties,
    private val adminDao: AdminDao,
    private val userDao: UserDao,
    private val gitHubService: GitHubService,
    private val weatherService: WeatherService,
    private val gcpService: GCPService,
    private val logger: LogDao,
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

    @GetMapping("/home", produces = [APPLICATION_JSON_VALUE])
    fun home(): HomeResponse {
        val weather = weatherService.getWeather()
        val settings = try {
            adminDao.getSettings()
        } catch (_: Exception) {
            null
        }
        return HomeResponse(weather, settings)
    }

    @GetMapping("/authenticate", produces = [APPLICATION_JSON_VALUE])
    fun authenticate(): ResponseEntity<String> = ResponseEntity.notFound().build()

    @PostMapping("/authenticate", produces = [APPLICATION_JSON_VALUE])
    fun authenticate(
        @RequestParam referrer: String?,
        @RequestHeader headers: Map<String, String>,
        request: HttpServletRequest,
        principal: Principal?
    ): AuthResponse {
        try {
            adminDao.visit(
                ip = request.remoteAddr,
                host = request.remoteHost,
                referer = referrer,
                agent = request.getHeader("User-Agent"),
                page = request.getHeader("referer")
            )

            principal?.let {
                userDao.updateLastSeen(it.name)
            }

            logger.info("Headers", headers.toJson())
        } catch (_: Exception) {
            // Database is probably down, ignore...
        }
        return AuthResponse(principal)
    }

    @PostMapping("/login")
    fun login(request: HttpServletRequest, response: HttpServletResponse): Boolean =
        request.authenticate(response).also {
            if (!it) logger.info("Login", "Incorrect credentials", request.remoteAddr)
        }

    @PostMapping("/database/{action}", produces = [APPLICATION_JSON_VALUE])
    fun gcpDatabase(@PathVariable action: String) = gcpService.handleDatabaseAction(action)

    @GetMapping("/qr", produces = [IMAGE_PNG_VALUE])
    fun qrCode(@RequestParam content: String?, @RequestParam size: Int?) =
        generateQRCodeImage(content ?: properties.baseUrl, size ?: 256)

    data class AuthResponse(
        val username: String?,
        val authenticated: Boolean,
        val roles: List<String>,
    ) {
        constructor(principal: Principal?) : this(
            username = principal?.name,
            authenticated = isLoggedIn(),
            roles = getUserRoles().map { it.split("_").last() },
        )
    }

    data class HomeResponse(
        val settings: Map<String, Any?>,
        val weather: Map<String, Any?>,
        val database: Boolean,
        val api: Boolean,
    ) {
        constructor(weather: Map<String, Any?>, settings: Map<String, Any?>? = null) : this(
            settings = settings ?: mapOf("countdownTarget" to now().plusDays(15).atStartOfDay()),
            weather = weather,
            database = settings != null,
            api = true,
        )
    }
}
