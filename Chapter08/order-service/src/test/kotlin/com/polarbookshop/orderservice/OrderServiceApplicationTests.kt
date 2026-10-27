package com.polarbookshop.orderservice

import com.ninjasquad.springmockk.MockkBean
import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.book.web.CatalogBookClient
import com.polarbookshop.orderservice.config.TestContainersConfiguration
import com.polarbookshop.orderservice.order.domain.Order
import com.polarbookshop.orderservice.order.domain.OrderStatus
import com.polarbookshop.orderservice.order.web.OrderRequest
import io.mockk.bdd.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

@AutoConfigureWebTestClient
@Import(TestContainersConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var bookClient: CatalogBookClient

    @Test
    fun `when customer orders available book, then order is accepted and appears in order list`() {
        // Given
        val isbn = "1234567893"
        val availableBook = Book(isbn, "Title", "Author", BigDecimal.valueOf(9.90))
        val expectedOrderStatus = OrderStatus.ACCEPTED
        val expectedOrderBookName = "${availableBook.title} - ${availableBook.author}"
        val orderRequest = OrderRequest(isbn, 1)

        given { bookClient.getBookByIsbn(isbn) } returns Mono.just(availableBook)

        // When: customer orders the book
        val createdOrder =
            webTestClient
                .post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody<Order>()
                .returnResult()
                .responseBody
                ?: fail("Expected the created order to be returned")

        // When: customer retrieves the order list
        val getOrderResponse =
            webTestClient
                .get()
                .uri("/orders")
                .exchange()

        // Then
        val actualOrders =
            getOrderResponse
                .expectStatus()
                .isOk()
                .expectBodyList<Order>()
                .returnResult()
                .responseBody
                .orEmpty()

        assertEquals(createdOrder.bookIsbn, isbn)
        assertEquals(expectedOrderBookName, createdOrder.bookName)
        assertEquals(orderRequest.quantity, createdOrder.quantity)
        assertEquals(expectedOrderStatus, createdOrder.status)

        assertThat(actualOrders).anyMatch { order ->
            createdOrder.id == order.id && createdOrder.bookIsbn == order.bookIsbn
        }
    }

    @Test
    fun `when customer orders book that does not exist, then order is rejected`() {
        // Given
        val nonExistingBookIsbn = "not-found-isbn"
        val orderRequest = OrderRequest(nonExistingBookIsbn, 1)

        val expectedOrderStatus = OrderStatus.REJECTED

        given { bookClient.getBookByIsbn(nonExistingBookIsbn) } returns Mono.empty()

        // When
        val createdOrderResponse =
            webTestClient
                .post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()

        // Then
        val createdOrder =
            createdOrderResponse
                .expectStatus()
                .isCreated()
                .expectBody<Order>()
                .returnResult()
                .responseBody
                ?: fail("Expected the created order to be returned")
        assertEquals(nonExistingBookIsbn, createdOrder.bookIsbn)
        assertEquals(expectedOrderStatus, createdOrder.status)
        assertNull(createdOrder.bookName)
        assertNull(createdOrder.bookPrice)
    }
}
