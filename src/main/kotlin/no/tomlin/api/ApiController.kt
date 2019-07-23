package no.tomlin.api

import no.tomlin.api.admin.AdminDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class ApiController {

    @Value("\${api.version}")
    private lateinit var version: String

    @Autowired
    private lateinit var adminDao: AdminDao

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("/version")
    fun version() = version

    @PostMapping("/visit")
    fun visit(@RequestParam referrer: String?, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        adminDao.visit(
            request.remoteAddr,
            request.remoteHost,
            referrer,
            request.getHeader("User-Agent"),
            request.getHeader("referer")
        )
        return request.authenticate(response)
    }
}
