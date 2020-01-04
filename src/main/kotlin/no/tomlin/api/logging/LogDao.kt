package no.tomlin.api.logging

import no.tomlin.api.common.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class LogDao {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun info(key: String, message: String) = log("[$key] $message")

    fun warn(exception: Exception, request: HttpServletRequest) =
        log("[Warn] ${exception::class.simpleName}", exception.message, request.servletPath)

    fun error(exception: Exception, request: HttpServletRequest) =
        log("[Error] ${exception::class.simpleName}", exception.message, request.servletPath)

    private fun log(message: String, details: String? = null, path: String? = null): Int =
        jdbcTemplate.update(
            "INSERT INTO ${Constants.TABLE_LOG} (`message`, `details`, `path`) VALUES (:message, :details, :path)",
            mapOf("message" to message, "details" to details, "path" to path))
}
