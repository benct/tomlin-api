package no.tomlin.api.config

import jakarta.servlet.http.HttpServletRequest
import no.tomlin.api.logging.LogDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.stereotype.Component

@Component
class AuthenticationFailureListener(private val logger: LogDao) : ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    @Autowired
    private lateinit var request: HttpServletRequest

    override fun onApplicationEvent(event: AuthenticationFailureBadCredentialsEvent) {
        logger.info("Login", "Incorrect credentials for ${event.authentication.name}", request.getHeader("x-forwarded-for"))
    }
}