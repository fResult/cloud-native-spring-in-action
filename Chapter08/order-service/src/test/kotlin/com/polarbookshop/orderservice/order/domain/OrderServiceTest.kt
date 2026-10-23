package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookClient
import io.mockk.bdd.given
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @MockK
    private lateinit var bookClient: BookClient

    @MockK
    private lateinit var orderRepository: OrderRepository

    @InjectMockKs
    private lateinit var orderService: OrderService

    @Test
    fun `when submit order and book is found, then save accepted order`() {
        // Given
        val isbn = "1234567890"
        val quantity = 3
        val book = Book(isbn, "Title", "Author", BigDecimal.valueOf(9.90))
        val order = Order.of(book.isbn, "${book.title} - ${book.author}", book.price, quantity, OrderStatus.ACCEPTED)
        val expectedOrder = order.copy(id = 1)
        given { bookClient.getBookByIsbn("1234567890") } returns Mono.just(book)
        given { orderRepository.save(any<Order>()) } returns Mono.just(expectedOrder)

        // When
        val actual = orderService.submitOrder(isbn, quantity)

        // Then
        StepVerifier.create(actual).expectNext(expectedOrder).verifyComplete()
    }

    @Test
    fun `when submit order and book isn't found, then save rejected order`() {
        // Given
        val notFoundIsbn = "not-found-isbn"
        val quantity = 3
        val notFoundException =
            WebClientResponseException.NotFound.create(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.reasonPhrase,
                HttpHeaders(),
                ByteArray(0),
                Charsets.UTF_8,
            )
        val expectedOrderStatus = OrderStatus.REJECTED
        val orderToCreate = Order.of(bookIsbn = notFoundIsbn, quantity = quantity, status = expectedOrderStatus)
        val expectedOrder = orderToCreate.copy(id = 1)

        given { bookClient.getBookByIsbn(notFoundIsbn) } returns Mono.error(notFoundException)
        given { orderRepository.save(orderToCreate) } returns Mono.just(expectedOrder)

        // When
        val actualOrder = orderService.submitOrder(notFoundIsbn, 3)

        // Then
        StepVerifier
            .create(actualOrder)
            .expectNext(expectedOrder)
            .verifyComplete()

        verify(exactly = 1) {
            bookClient.getBookByIsbn(notFoundIsbn)
            orderRepository.save(orderToCreate)
        }
    }
}
