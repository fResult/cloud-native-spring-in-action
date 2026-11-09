package com.polarbookshop.orderservice.order.web

import com.ninjasquad.springmockk.MockkBean
import com.polarbookshop.orderservice.order.domain.Order
import com.polarbookshop.orderservice.order.domain.OrderService
import com.polarbookshop.orderservice.order.domain.OrderStatus
import com.polarbookshop.orderservice.order.web.fixture.InvalidOrderRequestTestCase
import io.mockk.bdd.given
import io.mockk.called
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

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
        fun `when orders exist, then responds 200 with every order`() {
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

            verify(exactly = 1) { orderService.getAllOrders() }
        }

        @Test
        fun `when no orders exist, then responds 200 with empty list`() {
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

            verify(exactly = 1) { orderService.getAllOrders() }
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
            fun `when book is available, then responds 201 with an accepted order`() {
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

                verify(exactly = 1) { orderService.submitOrder(validOrderRequest.isbn, validOrderRequest.quantity) }
            }

            @Test
            fun `when book is not available, then responds 201 with rejected order`() {
                // Given
                val notFoundBookOrderRequest = OrderRequest("1234567890", 3)
                val expectedRejectedOrder =
                    rejectedOrderOf(isbn = notFoundBookOrderRequest.isbn!!, quantity = notFoundBookOrderRequest.quantity!!)
                given { orderService.submitOrder(notFoundBookOrderRequest.isbn, notFoundBookOrderRequest.quantity) } returns
                    Mono.just(expectedRejectedOrder)

                // When
                val response =
                    webClient
                        .post()
                        .uri("/orders")
                        .bodyValue(notFoundBookOrderRequest)
                        .exchange()

                // Then
                response
                    .expectStatus()
                    .isCreated()
                    .expectBody(Order::class.java)
                    .value { actualOrder ->
                        assertEquals(expectedRejectedOrder, actualOrder)
                    }

                verify(exactly = 1) { orderService.submitOrder(notFoundBookOrderRequest.isbn, notFoundBookOrderRequest.quantity) }
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class GivenInvalidOrderRequest {
            fun invalidOrderRequests(): List<InvalidOrderRequestTestCase> {
                val cannotBeMoreThanFiveItems = "You cannot order more than 5 items."
                val isbnMustBeDefined = "The book ISBN must be defined."

                return listOf(
                    InvalidOrderRequestTestCase(
                        description = "ISBN is missing but quantity is valid",
                        request = OrderRequest(isbn = null, quantity = 5),
                        expectedFieldErrors = mapOf("isbn" to isbnMustBeDefined),
                    ),
                    InvalidOrderRequestTestCase(
                        description = "ISBN is blank and quantity is higher than the maximum",
                        request = OrderRequest(isbn = "", quantity = 6),
                        expectedFieldErrors = mapOf("isbn" to isbnMustBeDefined, "quantity" to cannotBeMoreThanFiveItems),
                    ),
                )
            }

            @MethodSource("invalidOrderRequests")
            @ParameterizedTest(name = "when {0}, then responds 400 listing only the invalid fields and places no order")
            fun `when request body is invalid, then responds 400 listing only invalid fields and places no order`(
                testCase: InvalidOrderRequestTestCase,
            ) {
                // When
                val response = submit(testCase.request)

                // Then
                response
                    .expectStatus()
                    .isBadRequest()
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.status")
                    .isEqualTo(HttpStatus.BAD_REQUEST.value())
                    .jsonPath("$.arguments")
                    .isEqualTo(testCase.expectedFieldErrors)

                verify { orderService wasNot called }
            }
        }

        @Nested
        inner class GivenDatabaseRefusesOrder {
            @Test
            fun `when order conflicts with existing data, then responds 409 Conflict without leaking database details`() {
                // Given
                val expectedStatusCode = HttpStatus.CONFLICT
                val expectedTitle = "Resource Conflict"
                val expectedErrorMessage =
                    "The request could not be completed due to a conflict with the current state of the resource (e.g. duplicate key)."

                val orderPkConstraintKey = "orders_pkey"
                val duplicateOrder =
                    DataIntegrityViolationException("""duplicate key value violates unique constraint "$orderPkConstraintKey".""")

                given { orderService.submitOrder(validOrderRequest.isbn!!, validOrderRequest.quantity!!) } returns
                    Mono.error(duplicateOrder)

                // When
                val response = submit(validOrderRequest)

                // Then
                response
                    .expectStatus()
                    .isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.status")
                    .isEqualTo(expectedStatusCode.value())
                    .jsonPath("$.title")
                    .isEqualTo(expectedTitle)
                    .jsonPath("$.detail")
                    .isEqualTo(expectedErrorMessage)

                verify(exactly = 1) { orderService.submitOrder(validOrderRequest.isbn!!, validOrderRequest.quantity!!) }
            }

            @Test
            fun `when database fails for server-side reason, then responds 500 without leaking database details`() {
                // Given
                val expectedStatusCode = HttpStatus.INTERNAL_SERVER_ERROR
                val expectedErrorMessage = "Unexpected internal database error occurred."

                val orderTableName = "orders"
                val brokenSchema = InvalidDataAccessResourceUsageException("""relation "$orderTableName" does not exist""")
                given { orderService.submitOrder(validOrderRequest.isbn!!, validOrderRequest.quantity!!) } returns Mono.error(brokenSchema)

                // When
                val response = submit(validOrderRequest)

                // Then
                response
                    .expectStatus()
                    .isEqualTo(expectedStatusCode)
                    .expectBody()
                    .jsonPath("$.status")
                    .isEqualTo(expectedStatusCode.value())
                    .jsonPath("$.detail")
                    .isEqualTo(expectedErrorMessage)

                verify(exactly = 1) { orderService.submitOrder(validOrderRequest.isbn!!, validOrderRequest.quantity!!) }
            }
        }
    }
}
