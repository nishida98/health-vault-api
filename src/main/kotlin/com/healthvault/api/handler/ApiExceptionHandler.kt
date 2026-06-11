package com.healthvault.api.handler

import com.healthvault.api.dto.ErrorResponse
import com.healthvault.api.exception.DuplicateEmailException
import com.healthvault.api.exception.InvalidCredentialsException
import com.healthvault.api.exception.InvalidTokenException
import com.healthvault.api.exception.InvalidUserInputException
import com.healthvault.api.exception.UserAccountNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.BAD_REQUEST, exception.firstFieldErrorMessage())
    }

    @ExceptionHandler(InvalidUserInputException::class)
    fun handleInvalidInput(exception: InvalidUserInputException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.BAD_REQUEST, exception.message.orEmpty())
    }

    @ExceptionHandler(UserAccountNotFoundException::class)
    fun handleNotFound(exception: UserAccountNotFoundException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.NOT_FOUND, exception.message.orEmpty())
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(exception: DuplicateEmailException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.CONFLICT, exception.message.orEmpty())
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(exception: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.UNAUTHORIZED, exception.message.orEmpty())
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(exception: InvalidTokenException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.UNAUTHORIZED, exception.message.orEmpty())
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(exception: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.BAD_REQUEST, "${exception.name} is invalid.")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(exception: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return response(HttpStatus.BAD_REQUEST, "Request body is invalid.")
    }

    private fun response(status: HttpStatus, message: String): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(status).body(ErrorResponse(errorMessage = message))
    }

    private fun MethodArgumentNotValidException.firstFieldErrorMessage(): String {
        val error = bindingResult.fieldErrors.firstOrNull()
        return error?.messageWithFieldName() ?: "Invalid request body."
    }

    private fun FieldError.messageWithFieldName(): String {
        return "$field ${defaultMessage ?: "is invalid"}."
    }
}
