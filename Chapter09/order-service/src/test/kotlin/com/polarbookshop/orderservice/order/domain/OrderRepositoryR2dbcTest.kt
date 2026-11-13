package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.config.DataConfiguration
import com.polarbookshop.orderservice.config.TestContainersConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Flux
import reactor.kotlin.test.test
import reactor.test.StepVerifier
import kotlin.test.Test
import kotlin.test.assertEquals

@DataR2dbcTest
@Import(DataConfiguration::class, TestContainersConfiguration::class)
class OrderRepositoryR2dbcTest {
    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @Test
    fun `create rejected order`() {
        // Given
        val expectedRejectedOrder = Order.of(bookIsbn = "1234567890", quantity = 3, status = OrderStatus.REJECTED)

        // When
        val savedRejectedOrder = orderRepository.save(expectedRejectedOrder)

        // Then
        StepVerifier
            .create(savedRejectedOrder)
            .assertNext { actualRejectedOrder ->
                assertEquals(expectedRejectedOrder.status, actualRejectedOrder.status)
                assertEquals(expectedRejectedOrder.bookIsbn, actualRejectedOrder.bookIsbn)
            }.verifyComplete()
    }

    @Test
    fun `find orders by status, returns only dispatched orders`() {
        // Given
        val acceptedOrder = Order.of(bookIsbn = "1234567891", quantity = 5, status = OrderStatus.ACCEPTED)
        val dispatchedOrder1 = Order.of(bookIsbn = "1234567892", quantity = 1, status = OrderStatus.DISPATCHED)
        val dispatchedOrder2 = Order.of(bookIsbn = "1234567893", quantity = 2, status = OrderStatus.DISPATCHED)
        Flux
            .just(acceptedOrder, dispatchedOrder1, dispatchedOrder2)
            .flatMap(r2dbcEntityTemplate::insert)
            .then()
            .block()

        // When
        val savedOrders = orderRepository.findByStatus(OrderStatus.DISPATCHED)

        // Then
        savedOrders
            .collectList()
            .test()
            .assertNext { actualOrders ->
                assertEquals(2, actualOrders.size)

                val first = actualOrders.first { dispatchedOrder1.bookIsbn == it.bookIsbn }
                assertEquals(dispatchedOrder1.status, first.status)

                val second = actualOrders.first { dispatchedOrder2.bookIsbn == it.bookIsbn }
                assertEquals(dispatchedOrder2.status, second.status)
            }.verifyComplete()
    }
}
