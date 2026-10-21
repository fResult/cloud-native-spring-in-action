package com.polarbookshop.orderservice.order.web

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderRequestValidationTest {
    private lateinit var validator: Validator

    @BeforeAll
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        factory.use { validator =  it.validator }
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
}
