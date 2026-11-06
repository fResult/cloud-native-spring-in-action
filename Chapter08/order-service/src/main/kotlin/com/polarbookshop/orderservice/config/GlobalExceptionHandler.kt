package com.polarbookshop.orderservice.config

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
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
