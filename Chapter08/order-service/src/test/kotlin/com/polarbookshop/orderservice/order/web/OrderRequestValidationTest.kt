package com.polarbookshop.orderservice.order.web

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test
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

    @Test
    fun `when all fields correct, then validation succeeds`() {
        // Given
        val orderRequest = OrderRequest("1234567890", 1)

        // When
        val violations = validator.validate(orderRequest)

        // Then
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `when ISBN not defined, then validation fails`() {
        // Given
        val expectedErrorMessage = "The book ISBN must be defined."
        val orderRequest = OrderRequest("", 1)

        // When
        val validations = validator.validate(orderRequest)

        // Then
        assertEquals(1, validations.size)
        assertEquals(expectedErrorMessage, validations.first().message)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidQuantityCases")
    fun `when quantity is invalid, then validation fails`(
        testCase: String,
        invalidQuantity: Int?,
        expectedErrorMessage: String,
    ) {
        // Given
        val orderRequest = OrderRequest("1234567890", invalidQuantity)

        // When
        val violations = validator.validate(orderRequest)

        // Then
        assertEquals(1, violations.size)
        assertEquals(expectedErrorMessage, violations.single().message)
    }

    private fun invalidQuantityCases(): List<Arguments> =
        listOf(
            Arguments.of("quantity is not defined", null, "The book quantity must be defined."),
            Arguments.of("quantity is lower than min", 0, "You must order at least 1 item."),
            Arguments.of("quantity is higher than max", 7, "You cannot order more than 5 items"),
        )
}
