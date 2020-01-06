package no.tomlin.api

import no.tomlin.api.admin.AdminDao
import no.tomlin.api.common.Constants.ADMIN
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class ApiController {

    @Value("\${api.name}")
    private lateinit var name: String

    @Value("\${api.version}")
    private lateinit var version: String

    @Value("\${api.baseUrl}")
    private lateinit var baseUrl: String

    @Autowired
    private lateinit var adminDao: AdminDao

    @GetMapping("/")
    fun base() = mapOf("site" to name, "version" to version, "baseUrl" to baseUrl)

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("/version")
    fun version() = version

    @PostMapping("/authenticate")
    fun authenticate(@RequestParam referrer: String?, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        adminDao.visit(
            request.remoteAddr,
            request.remoteHost,
            referrer,
            request.getHeader("User-Agent"),
            request.getHeader("referer")
        )
        return request.authenticate(response)
    }

    @PostMapping("/login")
    fun login(request: HttpServletRequest, response: HttpServletResponse) = request.authenticate(response)
}
