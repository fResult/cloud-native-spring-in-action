package com.polarbookshop.orderservice.config

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.NonTransientDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(ex: WebExchangeBindException): ResponseEntity<ProblemDetail> {
        val validationErrors =
            ex.bindingResult.fieldErrors
                .associate(::toErrorFieldAndMessage)
                .toSortedMap()

        val detail =
            ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
                setProperty("arguments", validationErrors)
            }

        log.info("Method argument validation failed: {}", validationErrors)

        return ResponseEntity.of(detail).build()
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ProblemDetail> {
        val validationErrors =
            ex.constraintViolations
                .associate(::toErrorFieldAndMessage)
                .toSortedMap()

        val detail =
            ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
                setProperty("arguments", validationErrors)
            }

        log.info("Constraint violation: {}", validationErrors)

        return ResponseEntity.of(detail).build()
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ProblemDetail> {
        val errorMessage =
            "The request could not be completed due to a conflict with the current state of the resource (e.g. duplicate key)."
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, errorMessage).apply { title = "Resource Conflict" }

        log.info("Database integrity conflict: {}", ex.message)

        return ResponseEntity.of(detail).build()
    }

    @ExceptionHandler(NonTransientDataAccessException::class)
    fun handleNonTransientDataAccess(ex: NonTransientDataAccessException): ResponseEntity<ProblemDetail> {
        val errorMessage = "Unexpected internal database error occurred."
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage)

        log.warn("Database persistence error (Server-side issue):", ex)

        return ResponseEntity.of(detail).build()
    }

    private fun toErrorFieldAndMessage(error: FieldError): Pair<String, String> {
        val fieldName = error.field
        val errorMessage = error.defaultMessage ?: "Invalid value"

        return fieldName to errorMessage
    }

    private fun toErrorFieldAndMessage(violation: ConstraintViolation<*>): Pair<String, String> {
        val paramName = violation.propertyPath.lastOrNull()?.name ?: "unknown"
        val errorMessage = violation.message ?: "Invalid value"

        return paramName to errorMessage
    }
}
