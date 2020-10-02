package no.tomlin.api

import no.tomlin.api.admin.dao.AdminDao
import no.tomlin.api.config.ApiProperties
import no.tomlin.api.github.GitHubService
import no.tomlin.api.admin.dao.UserDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class ApiController {

    @Autowired
    private lateinit var properties: ApiProperties

    @Autowired
    private lateinit var adminDao: AdminDao

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var gitHubService: GitHubService

    @GetMapping("/")
    fun base() = mapOf("site" to properties.name, "version" to properties.version, "baseUrl" to properties.baseUrl)

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("/version")
    fun version() = properties.version

    @GetMapping("/github")
    fun github() = gitHubService.getUserData()

    @PostMapping("/authenticate")
    fun authenticate(@RequestParam referrer: String?, request: HttpServletRequest, principal: Principal?): Map<String, Any?> {
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

        return mapOf(
            "authenticated" to (SecurityContextHolder.getContext().authentication?.isAuthenticated ?: false),
            "settings" to adminDao.getSettings(),
            "username" to principal?.name
        )
    }

    @PostMapping("/login")
    fun login(request: HttpServletRequest, response: HttpServletResponse) = request.authenticate(response)
}
