package no.tomlin.api.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST
import no.tomlin.api.common.Extensions.nullIfBlank
import no.tomlin.api.logging.LogDao
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ClientIdInterceptor(private val logger: LogDao) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val clientId: String? = request.getHeader(CLIENT_ID) ?: request.getParameter("clientId")
        if (!validClientIds.contains(clientId)) {
            logger.info(
                key = "Interceptor",
                message = clientId.nullIfBlank()?.let { "Unknown client-id '$clientId'" } ?: "Missing client-id",
                details = "IP: ${request.remoteAddr}, Host: ${request.remoteHost}, " +
                    "Agent: ${request.getHeader("User-Agent")}, Referer: ${request.getHeader("referer")}",
                path = request.servletPath + request.queryString.nullIfBlank()?.let { "?$it" },
            )

            response.sendError(SC_BAD_REQUEST, "Missing or invalid header [$CLIENT_ID].")
            return false
        }
        return true
    }

    private companion object {
        const val CLIENT_ID = "Client-Id"
        val validClientIds = listOf("tomlin-web", "iata-utils")
    }
}