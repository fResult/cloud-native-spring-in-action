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
    fun `when submitting an order and the book is found, then saves an accepted`() {
        // Given
        val isbn = "1234567890"
        val quantity = 3
        val book = Book(isbn, "Title", "Author", BigDecimal.valueOf(9.90))
        val orderToSave = Order.of(book.isbn, "${book.title} - ${book.author}", book.price, quantity, OrderStatus.ACCEPTED)
        val savedOrder = orderToSave.copy(id = 1)

        given { bookClient.getBookByIsbn(isbn) } returns Mono.just(book)
        given { orderRepository.save(orderToSave) } returns Mono.just(savedOrder)

        // When
        val result = orderService.submitOrder(isbn, quantity)

        // Then
        StepVerifier.create(result).expectNext(savedOrder).verifyComplete()
        verify(exactly = 1) {
            bookClient.getBookByIsbn(isbn)
            orderRepository.save(orderToSave)
        }
    }

    @Test
    fun `when submitting an order and the book isn't found, then saves rejected order`() {
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
        val orderToSave = Order.of(bookIsbn = notFoundIsbn, quantity = quantity, status = OrderStatus.REJECTED)
        val savedOrder = orderToSave.copy(id = 1)

        given { bookClient.getBookByIsbn(notFoundIsbn) } returns Mono.error(notFoundException)
        given { orderRepository.save(orderToSave) } returns Mono.just(savedOrder)

        // When
        val result = orderService.submitOrder(notFoundIsbn, quantity)

        // Then
        StepVerifier.create(result).expectNext(savedOrder).verifyComplete()
        verify(exactly = 1) {
            bookClient.getBookByIsbn(notFoundIsbn)
            orderRepository.save(orderToSave)
        }
    }
}
