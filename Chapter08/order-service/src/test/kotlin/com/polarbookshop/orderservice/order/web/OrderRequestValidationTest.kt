package com.polarbookshop.orderservice.order.web

import com.polarbookshop.orderservice.order.web.fixture.InvalidOrderRequestTestCase
import com.polarbookshop.orderservice.order.web.fixture.ValidOrderRequestTestCase
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderRequestValidationTest {
    private lateinit var validator: Validator

    @BeforeAll
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        factory.use { validator = it.validator }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GivenValidOrderRequest {
        @ParameterizedTest(name = "when {0}, then validation succeeds")
        @MethodSource("validOrderRequestCases")
        fun `when request satisfies all constraints, then validation succeeds`(testCase: ValidOrderRequestTestCase) {
            // Given
            val orderRequest = testCase.request

            // When
            val violations = validator.validate(orderRequest)

            // Then
            assertTrue(violations.isEmpty(), "Expected no validation errors for ${testCase.request}")
        }

        fun validOrderRequestCases() =
            listOf(
                ValidOrderRequestTestCase(
                    description = "quantity is at the minimum boundary",
                    request = OrderRequest("1234567890", 1),
                ),
                ValidOrderRequestTestCase(
                    description = "quantity is a valid middle value",
                    request = OrderRequest("1234567890", 3),
                ),
                ValidOrderRequestTestCase(
                    description = "quantity is at the maximum boundary",
                    request = OrderRequest("1234567890", 5),
                ),
            )
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GivenInvalidOrderRequest {
        fun invalidOrderRequestCases(): List<InvalidOrderRequestTestCase> {
            val isbnMustBeDefined = "The book ISBN must be defined."
            val quantityMustBeDefined = "The book quantity must be defined."
            val mustBeAtLeastOneItem = "You must order at least 1 item."
            val cannotBeMoreThanFiveItems = "You cannot order more than 5 items."

            return listOf(
                InvalidOrderRequestTestCase(
                    description = "ISBN is blank but quantity is valid",
                    request = OrderRequest("", 3),
                    expectedFieldErrors = mapOf("isbn" to isbnMustBeDefined),
                ),
                InvalidOrderRequestTestCase(
                    description = "ISBN is missing but quantity is valid",
                    request = OrderRequest(isbn = null, quantity = 5),
                    expectedFieldErrors = mapOf("isbn" to isbnMustBeDefined),
                ),
                InvalidOrderRequestTestCase(
                    description = "ISBN contains only whitespace but quantity is valid",
                    request = OrderRequest("    ", 3),
                    expectedFieldErrors = mapOf("isbn" to isbnMustBeDefined),
                ),
                InvalidOrderRequestTestCase(
                    description = "ISBN is valid but quantity is missing",
                    request = OrderRequest(isbn = "1234567890", quantity = null),
                    expectedFieldErrors = mapOf("quantity" to quantityMustBeDefined),
                ),
                InvalidOrderRequestTestCase(
                    description = "ISBN is valid but quantity is lower than the minimum",
                    request = OrderRequest(isbn = "1234567890", quantity = 0),
                    expectedFieldErrors = mapOf("quantity" to mustBeAtLeastOneItem),
                ),
                InvalidOrderRequestTestCase(
                    description = "ISBN is valid but quantity is higher than the maximum",
                    request = OrderRequest(isbn = "1234567890", quantity = 6),
                    expectedFieldErrors = mapOf("quantity" to cannotBeMoreThanFiveItems),
                ),
                InvalidOrderRequestTestCase(
                    description = "ISBN is blank and quantity is higher than the maximum",
                    request = OrderRequest(isbn = "", quantity = 6),
                    expectedFieldErrors = mapOf("isbn" to isbnMustBeDefined, "quantity" to cannotBeMoreThanFiveItems),
                ),
            )
        }

        @ParameterizedTest(name = "when {0}, then reports the expected validation errors")
        @MethodSource("invalidOrderRequestCases")
        fun `when order request violates constraints, then validation reports the expected field errors`(
            testCase: InvalidOrderRequestTestCase,
        ) {
            // Given
            val expectedFieldErrors = testCase.expectedFieldErrors
            val orderRequest = testCase.request

            // When
            val violations = validator.validate(orderRequest)

            // Then
            violations shouldMatchFieldErrorsWith expectedFieldErrors
        }

        private infix fun <T> Set<ConstraintViolation<T>>.shouldMatchFieldErrorsWith(expectedFieldErrors: Map<String, String>) {
            val actualFieldErrors = this.associate { it.propertyPath.toString() to it.message }.toSortedMap()

            assertEquals(expectedFieldErrors, actualFieldErrors)
        }
    }
}
