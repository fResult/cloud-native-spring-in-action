package com.polarbookshop.orderservice.order.web

import com.ninjasquad.springmockk.MockkBean
import com.polarbookshop.orderservice.order.domain.Order
import com.polarbookshop.orderservice.order.domain.OrderService
import com.polarbookshop.orderservice.order.domain.OrderStatus
import io.mockk.bdd.given
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
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
        private fun rejectedOrderOf(
            isbn: String,
            quantity: Int,
        ): Order = Order.of(bookIsbn = isbn, quantity = quantity, status = OrderStatus.REJECTED)
    }

    @Nested
    inner class GetAllOrders {
        @Test
        fun `when orders exists, then responds 200 with every order`() {
            // Given
            val acceptedOrder = Order.of("1234567891", "Title - Author", BigDecimal("9.90"), 1, OrderStatus.ACCEPTED)
            val rejectedOrder = rejectedOrderOf(isbn = "1234567899", quantity = 2)

            given { orderService.getAllOrders() } returns Flux.just(acceptedOrder, rejectedOrder)

            // When
            val response = webClient.get().uri("/orders").exchange()

            // Then
            val orders =
                response
                    .expectStatus()
                    .isOk()
                    .expectBodyList<Order>()
                    .returnResult()
                    .responseBody
                    .orEmpty()

            assertEquals(2, orders.size)
            assertEquals(acceptedOrder, orders.first())
            assertEquals(rejectedOrder, orders[1])
        }

        @Test
        fun `when orders do not exists, then responds 200 with empty list`() {
            // Given
            given { orderService.getAllOrders() } returns Flux.empty()

            // When
            val response = webClient.get().uri("/orders").exchange()

            // Then
            val orders =
                response
                    .expectStatus()
                    .isOk()
                    .expectBodyList<Order>()
                    .returnResult()
                    .responseBody
                    .orEmpty()

            assertEquals(0, orders.size)
        }
    }

    @Nested
    inner class SubmitOrder {
        private val validOrderRequest = OrderRequest(isbn = "1234567891", quantity = 3)

        private fun submit(orderRequest: OrderRequest) =
            webClient
                .post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()

        @Nested
        inner class GivenValidOrderRequest {
            @Test
            fun `when book is available, then reponds 201 with an accepted order`() {
                // Given
                val expectedAcceptedOrder =
                    Order.of(
                        validOrderRequest.isbn!!,
                        "Title - Author",
                        BigDecimal("12.90"),
                        validOrderRequest.quantity!!,
                        OrderStatus.ACCEPTED,
                    )
                given { orderService.submitOrder(validOrderRequest.isbn, validOrderRequest.quantity) } returns
                    Mono.just(expectedAcceptedOrder)

                // When
                val response = submit(validOrderRequest)

                // Then
                response
                    .expectStatus()
                    .isCreated()
                    .expectBody(Order::class.java)
                    .value { actualOrder ->
                        assertEquals(expectedAcceptedOrder, actualOrder)
                    }
            }

            @Test
            fun `when book is not available, then responds 201 with rejected order`() {
                // Given
                val orderRequest = OrderRequest("1234567890", 3)
                val expectedOrder = rejectedOrderOf(isbn = orderRequest.isbn!!, quantity = orderRequest.quantity!!)
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
                    .isCreated()
                    .expectBody(Order::class.java)
                    .value { actualOrder ->
                        assertNotNull(actualOrder)
                        assertEquals(expectedOrder.status, actualOrder.status)
                    }
            }
        }
    }
}
