package com.polarbookshop.orderservice.order.web

import io.mockk.core.ValueClassSupport.boxedValue
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import kotlin.test.Test

@JsonTest
class OrderRequestJsonTest {
    @Autowired
    private lateinit var json: JacksonTester<OrderRequest>

    @Test
    fun `test deserialize`() {
        // Given
        val isbn = "1234567890"
        val quantity = 1
        val expectedOrderRequest = OrderRequest(isbn, quantity)
        val jsonContent =
            """
            {
              "isbn": ${isbn.boxedValue},
              "quantity": $quantity
            }
            """.trimIndent()

        // When
        val actualOrderRequest = json.parseObject(jsonContent)

        // Then
        assertThat(actualOrderRequest)
            .usingRecursiveComparison()
            .isEqualTo(expectedOrderRequest)
    }
}
