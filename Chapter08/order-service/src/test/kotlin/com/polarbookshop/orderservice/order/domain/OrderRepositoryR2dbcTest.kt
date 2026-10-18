package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.config.DataConfiguration
import com.polarbookshop.orderservice.config.TestContainersConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier
import kotlin.test.Test

@DataR2dbcTest
@Import(DataConfiguration::class, TestContainersConfiguration::class)
class OrderRepositoryR2dbcTest {
    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Test
    fun `create rejected order`() {
        // Given
        val expectedRejectedOrder = Order.of(bookIsbn = "1234567890", quantity = 3, status = OrderStatus.REJECTED)

        // When
        val savedRejectedOrder = orderRepository.save(expectedRejectedOrder)

        // Then
        StepVerifier
            .create(savedRejectedOrder)
            .expectNextMatches { actualRejectedOrder ->
                actualRejectedOrder.status == OrderStatus.REJECTED &&
                    expectedRejectedOrder.bookIsbn == actualRejectedOrder.bookIsbn
            }.verifyComplete()
    }
}
