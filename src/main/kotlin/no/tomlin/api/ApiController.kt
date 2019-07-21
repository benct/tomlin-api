package no.tomlin.api

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @GetMapping("/ping")
    fun ping() = "pong"

    @Secured(ADMIN)
    @GetMapping("/db")
    fun dbTest() = jdbcTemplate.queryForList("SELECT * FROM tomlin_flight")

    @Secured(USER, ADMIN)
    @GetMapping("/auth")
    fun auth() = "access granted"
}
