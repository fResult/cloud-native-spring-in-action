package com.polarbookshop.orderservice.order.web

import com.polarbookshop.orderservice.order.domain.Order
import com.polarbookshop.orderservice.order.domain.OrderStatus
import io.mockk.core.ValueClassSupport.boxedValue
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertNotNull

@JsonTest
class OrderJsonTest {
    @Autowired
    private lateinit var json: JacksonTester<Order>

    @Test
    fun `test serialize`() {
        // Given
        val now = Instant.now()
        val order = Order(394, "1234567890", "Book Name", BigDecimal.valueOf(9.90), 1, OrderStatus.ACCEPTED, now, now, 26)

        // When
        val jsonContent = json.write(order)

        // Then
        assertNotNull(order.id)
        assertNotNull(order.bookPrice)

        assertThat(jsonContent)
            .extractingJsonPathNumberValue("@.id")
            .isEqualTo(order.id.toInt())
        assertThat(jsonContent)
            .extractingJsonPathStringValue("@.bookIsbn")
            .isEqualTo(order.bookIsbn)
        assertThat(jsonContent)
            .extractingJsonPathStringValue("@.bookName")
            .isEqualTo(order.bookName)
        assertThat(jsonContent)
            .extractingJsonPathNumberValue("@.bookPrice")
            .isEqualTo(order.bookPrice.toDouble())
        assertThat(jsonContent)
            .extractingJsonPathNumberValue("@.quantity")
            .isEqualTo(order.quantity)
        assertThat(jsonContent)
            .extractingJsonPathStringValue("@.status")
            .isEqualTo(order.status.name)
        assertThat(jsonContent)
            .extractingJsonPathStringValue("@.createdDate")
            .isEqualTo(order.createdDate.toString())
        assertThat(jsonContent)
            .extractingJsonPathStringValue("@.lastModifiedDate")
            .isEqualTo(order.lastModifiedDate.toString())
        assertThat(jsonContent)
            .extractingJsonPathNumberValue("@.version")
            .isEqualTo(order.version)
    }
}
