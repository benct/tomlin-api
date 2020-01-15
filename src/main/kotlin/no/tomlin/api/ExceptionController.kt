package no.tomlin.api

import no.tomlin.api.admin.SettingsController.SettingNotFoundException
import no.tomlin.api.logging.LogDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
class ExceptionController {

    @Autowired
    lateinit var logger: LogDao

    @ExceptionHandler(
        EmptyResultDataAccessException::class,
        IncorrectResultSizeDataAccessException::class,
        SettingNotFoundException::class)
    fun handleNotFound(exception: Exception, request: HttpServletRequest): ResponseEntity<Any> {
        logger.warn(exception, request)
        return ResponseEntity(ErrorResponse(NOT_FOUND, exception, request), NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(exception: Exception, request: HttpServletRequest) {
        logger.error(exception, request)
        throw exception
    }

    data class ErrorResponse(
        val status: Int,
        val error: String,
        val message: String?,
        val path: String? = null,
        val timestamp: LocalDateTime = now()
    ) {
        constructor(status: HttpStatus, exception: Exception, request: HttpServletRequest) :
            this(status.value(), status.reasonPhrase, exception.message, request.servletPath)
    }
}