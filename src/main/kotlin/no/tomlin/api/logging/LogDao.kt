package no.tomlin.api.logging

import jakarta.servlet.http.HttpServletRequest
import no.tomlin.api.db.Extensions.update
import no.tomlin.api.db.Insert
import no.tomlin.api.db.Table.TABLE_LOG
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class LogDao(private val jdbc: NamedParameterJdbcTemplate) {

    fun info(key: String, message: String, details: String? = null, path: String? = null) =
        log("[$key] $message", details, path)

    fun warn(exception: Exception, request: HttpServletRequest? = null) =
        log("[Warn] ${exception::class.simpleName}", exception.message, buildPath(request))

    fun error(exception: Exception, request: HttpServletRequest? = null) =
        log("[Error] ${exception::class.simpleName}", exception.message, buildPath(request))

    fun requestInfo(key: String, message: String, request: HttpServletRequest) =
        log("[$key] $message", buildHeaderInfo(request), buildPath(request))

    private fun log(message: String, details: String? = null, path: String? = null): Boolean = jdbc.update(
        Insert(TABLE_LOG).data("message" to message, "details" to details, "path" to path)
    )

    private companion object {
        fun buildPath(request: HttpServletRequest?): String? =
            request?.servletPath?.plus(request.queryString?.let { "?$it" }.orEmpty())

        fun buildHeaderInfo(request: HttpServletRequest): String =
            "IP/Host: ${request.getHeader("x-forwarded-for") ?: request.remoteAddr} / ${request.remoteHost}\n" +
                "Agent: ${request.getHeader("user-agent")}" +
                request.getHeader("referer")?.let { "\nReferer: $it" }.orEmpty()
    }
}
