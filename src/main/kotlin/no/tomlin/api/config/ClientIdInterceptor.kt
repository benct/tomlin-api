package no.tomlin.api.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.tomlin.api.logging.LogDao
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ClientIdInterceptor(private val logger: LogDao) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val clientId: String? = request.getHeader("Client-Id")
        if (!validClientIds.contains(clientId)) {
            val requestInfo = "IP: ${request.remoteAddr}, Host: ${request.remoteHost}, " +
                "Agent: ${request.getHeader("User-Agent")}, Referer: ${request.getHeader("referer")}"
            logger.info("Interceptor", "Unknown client: $clientId ($requestInfo)")
        }
        return true
    }

    private companion object {
        val validClientIds = listOf("tomlin-web", "iata-utils")
    }
}