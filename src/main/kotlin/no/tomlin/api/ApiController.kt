package no.tomlin.api

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController {

    @Value("\${api.version}")
    private lateinit var version: String

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @GetMapping("/ping")
    fun ping() = "pong"

    @GetMapping("/version")
    fun version() = version

    @Secured(USER, ADMIN)
    @GetMapping("/auth")
    fun auth() = "access granted"
}
