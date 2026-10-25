package com.polarbookshop.orderservice.order.web

import com.ninjasquad.springmockk.MockkBean
import com.polarbookshop.orderservice.order.domain.Order
import com.polarbookshop.orderservice.order.domain.OrderService
import com.polarbookshop.orderservice.order.domain.OrderStatus
import io.mockk.bdd.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@WebFluxTest(OrderController::class)
class OrderControllerWebFluxTest {
    @Autowired
    private lateinit var webClient: WebTestClient

    @MockkBean
    private lateinit var orderService: OrderService

    companion object {
        private fun buildRejectedOrder(
            isbn: String,
            quantity: Int,
        ): Order = Order.of(bookIsbn = isbn, quantity = quantity, status = OrderStatus.REJECTED)
    }

    @Test
    fun `when book not available, then reject order`() {
        // Given
        val orderRequest = OrderRequest("1234567890", 3)
        val expectedOrder = buildRejectedOrder(isbn = orderRequest.isbn!!, quantity = orderRequest.quantity!!)
        given { orderService.submitOrder(orderRequest.isbn, orderRequest.quantity) } returns Mono.just(expectedOrder)

        // When
        val response =
            webClient
                .post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()

        // Then
        response
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(Order::class.java)
            .value { actualOrder ->
                assertNotNull(actualOrder)
                assertEquals(expectedOrder.status, actualOrder.status)
            }
    }
}
