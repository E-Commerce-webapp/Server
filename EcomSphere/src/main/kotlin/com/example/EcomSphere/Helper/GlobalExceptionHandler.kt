package com.example.EcomSphere.Helper

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult
            .fieldErrors
            .associate { error -> 
                error.field to (error.defaultMessage ?: "Validation error")
            }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Validation failed",
            path = request.getDescription(false).substring(4), // Remove "uri=" prefix
            timestamp = LocalDateTime.now(),
            errors = errors
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ForbiddenActionException::class)
    fun handleForbidden(e: ForbiddenActionException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(createErrorResponse(e, HttpStatus.FORBIDDEN, request))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(e, HttpStatus.BAD_REQUEST, request))
    }

    @ExceptionHandler(EmailAlreadyVerifiedException::class, AlreadyASellerException::class)
    fun handleBadRequestExceptions(e: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(e, HttpStatus.BAD_REQUEST, request))
    }

    @ExceptionHandler(NotFoundActionException::class)
    fun handleNotFound(e: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(createErrorResponse(e, HttpStatus.NOT_FOUND, request))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request))
    }

    private fun createErrorResponse(
        e: Exception,
        status: HttpStatus,
        request: WebRequest
    ): ErrorResponse {
        return ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = e.message ?: "An error occurred",
            path = request.getDescription(false).substring(4),
            timestamp = LocalDateTime.now()
        )
    }
}

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: LocalDateTime,
    val errors: Map<String, String>? = null
)