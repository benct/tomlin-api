package no.tomlin.api.config

import jakarta.servlet.http.HttpServletRequest
import no.tomlin.api.common.Extensions.ifNotBlank
import no.tomlin.api.logging.LogDao
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.stereotype.Component

@Component
class AuthenticationFailureListener(private val logger: LogDao, private val request: HttpServletRequest) :
    ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    override fun onApplicationEvent(event: AuthenticationFailureBadCredentialsEvent) {
        logger.requestInfo("Login", "Incorrect credentials${event.authentication.name.ifNotBlank { " for '$it'" }}", request)
    }
}