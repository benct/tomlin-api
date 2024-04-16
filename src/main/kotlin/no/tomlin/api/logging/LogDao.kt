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
        log("[Warn] ${exception::class.simpleName}", exception.message, request?.servletPath)

    fun error(exception: Exception, request: HttpServletRequest? = null) =
        log("[Error] ${exception::class.simpleName}", exception.message, request?.servletPath)

    private fun log(message: String, details: String? = null, path: String? = null): Boolean = jdbc.update(
        Insert(TABLE_LOG).data("message" to message, "details" to details, "path" to path)
    )
}
