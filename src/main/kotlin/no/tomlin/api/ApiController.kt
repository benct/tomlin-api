package no.tomlin.api

import no.tomlin.api.admin.AdminDao
import no.tomlin.api.config.ApiProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class ApiController {

    @Autowired
    private lateinit var properties: ApiProperties

    @Autowired
    private lateinit var adminDao: AdminDao

    @GetMapping("/")
    fun base() = mapOf("site" to properties.name, "version" to properties.version, "baseUrl" to properties.baseUrl)

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("/version")
    fun version() = properties.version

    @PostMapping("/authenticate")
    fun authenticate(@RequestParam referrer: String?, request: HttpServletRequest): Map<String, Any?> {
        adminDao.visit(
            request.remoteAddr,
            request.remoteHost,
            referrer,
            request.getHeader("User-Agent"),
            request.getHeader("referer")
        )

        return mapOf(
            "authenticated" to (SecurityContextHolder.getContext().authentication?.isAuthenticated ?: false),
            "settings" to adminDao.getSettings()
        )
    }

    @PostMapping("/login")
    fun login(request: HttpServletRequest, response: HttpServletResponse) = request.authenticate(response)
}
