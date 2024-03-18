package no.tomlin.api.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.tomlin.api.logging.LogDao
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ClientIdInterceptor(private val logger: LogDao) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val clientId: String? = request.getHeader(CLIENT_ID) ?: request.getParameter("clientId")
        if (!validClientIds.contains(clientId)) {
            val requestInfo = "IP: ${request.remoteAddr}, Host: ${request.remoteHost}, " +
                "Agent: ${request.getHeader("User-Agent")}, Referer: ${request.getHeader("referer")}"
            logger.info("Interceptor", "Unknown client: $clientId ($requestInfo)")

            response.sendError(BAD_REQUEST.value(), "Missing required header [$CLIENT_ID].")
            return false
        }
        return true
    }

    private companion object {
        const val CLIENT_ID = "Client-Id"
        val validClientIds = listOf("tomlin-web", "iata-utils")
    }
}