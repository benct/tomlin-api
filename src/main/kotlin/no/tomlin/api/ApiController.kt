package no.tomlin.api

import no.tomlin.api.common.Constants.ADMIN
import no.tomlin.api.common.Constants.USER
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController {

    @GetMapping("/ping")
    fun ping() = "pong"

    @Secured(USER, ADMIN)
    @GetMapping("/auth")
    fun auth() = "access granted"
}
