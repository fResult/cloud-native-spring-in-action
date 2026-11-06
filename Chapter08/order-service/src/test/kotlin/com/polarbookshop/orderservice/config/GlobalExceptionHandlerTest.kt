package com.polarbookshop.orderservice.config

import io.mockk.bdd.given
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.support.WebExchangeBindException
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {
    private val expectionHandler = GlobalExceptionHandler()

    @Nested
    inner class GivenCustomerSendsInvalidOrderBody {
        @Test
        fun `when several fields are invalid, then responds 400 listing every field with its reason in alphabetical order`() {
            // Given
            val quantityField = "quantity"
            val quantityErrorMessage = "Quantity must be at most 5"
            val isbnField = "isbn"
            val isbnErrorMessage = "The book ISBN must be defined"

            val expectedFieldsAndMessages =
                listOf(
                    isbnField to isbnErrorMessage,
                    quantityField to quantityErrorMessage,
                )
            val invalidBody =
                requestBodyRejectedFor(
                    fieldError(fieldName = quantityField, reason = quantityErrorMessage),
                    fieldError(fieldName = isbnField, reason = isbnErrorMessage),
                )

            // When
            val response = expectionHandler.handleWebExchangeBindException(invalidBody)

            // Then
            assertResponseWith(HttpStatus.BAD_REQUEST, response)
            assertEquals(expectedFieldsAndMessages, response.reportedFieldErrors())
        }
    }

    private fun assertResponseWith(
        expectedStatus: HttpStatus,
        response: ResponseEntity<ProblemDetail>,
    ) {
        assertEquals(expectedStatus, response.statusCode)
        assertEquals(
            expectedStatus.value(),
            response.body?.status,
            "ProblemDetail status should be ${expectedStatus.value()} but was ${response.body?.status}",
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun ResponseEntity<ProblemDetail>.reportedFieldErrors(): List<Pair<String, String>> =
        (this.body?.properties?.getOrDefault("arguments", emptyMap<String, String>()) as Map<String, String>).toList()

    private fun fieldError(
        fieldName: String,
        reason: String,
    ) = FieldError("orderRequest", fieldName, reason)

    private fun requestBodyRejectedFor(vararg errors: FieldError): WebExchangeBindException =
        mockk { given { bindingResult.fieldErrors } returns errors.toList() }
}
