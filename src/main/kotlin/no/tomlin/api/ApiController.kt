package no.tomlin.api

import no.tomlin.api.admin.dao.AdminDao
import no.tomlin.api.admin.dao.UserDao
import no.tomlin.api.admin.service.GCPService
import no.tomlin.api.config.ApiProperties
import no.tomlin.api.github.GitHubService
import no.tomlin.api.weather.WeatherService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDate.now
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class ApiController {

    // TODO: Move all lateinit wiring to constructors
    @Autowired
    private lateinit var properties: ApiProperties

    @Autowired
    private lateinit var adminDao: AdminDao

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var gitHubService: GitHubService

    @Autowired
    private lateinit var weatherService: WeatherService

    @Autowired
    private lateinit var gcpService: GCPService

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
    fun authenticate(@RequestParam referrer: String?, request: HttpServletRequest, principal: Principal?): AuthResponse =
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

    @GetMapping("/database/{action}", produces = [APPLICATION_JSON_VALUE])
    fun gcpDatabase(@PathVariable action: String) = gcpService.handleDatabaseStartStop(action)

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
