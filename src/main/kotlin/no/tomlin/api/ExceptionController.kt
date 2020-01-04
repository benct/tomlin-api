package no.tomlin.api

import no.tomlin.api.admin.AdminDao
import no.tomlin.api.admin.SettingsController.SettingNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionController {

    @Autowired
    lateinit var adminDao: AdminDao

    @ExceptionHandler(
        EmptyResultDataAccessException::class,
        IncorrectResultSizeDataAccessException::class,
        SettingNotFoundException::class)
    fun handleNotFound(exception: Exception, request: HttpServletRequest): ResponseEntity<Any> {
        adminDao.log("[Warn] ${exception::class.simpleName}", exception.message, request.servletPath)
        return ResponseEntity(ErrorResponse(NOT_FOUND, exception, request), NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(exception: Exception, request: HttpServletRequest) {
        adminDao.log("[Error] ${exception::class.simpleName}", exception.message, request.servletPath)
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
