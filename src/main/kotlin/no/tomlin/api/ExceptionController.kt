package no.tomlin.api

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionController {

    @ExceptionHandler(EmptyResultDataAccessException::class, IncorrectResultSizeDataAccessException::class)
    fun notFound(exception: Exception, request: HttpServletRequest) =
        ResponseEntity(ErrorResponse(NOT_FOUND, exception, request), NOT_FOUND)

    @ExceptionHandler(Exception::class)
    fun generalException(exception: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        // log
        return ResponseEntity(ErrorResponse(INTERNAL_SERVER_ERROR, exception, request), INTERNAL_SERVER_ERROR)
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
