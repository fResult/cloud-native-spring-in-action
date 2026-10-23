package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookClient
import io.mockk.bdd.given
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.netty.handler.timeout.ReadTimeoutException
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.net.ConnectException
import java.net.URI
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
    fun `when getting all orders, then returns all orders from repository`() {
        // Given
        val order1 = Order.of("1234567891", "Book Name 1", BigDecimal.TEN, 2, OrderStatus.DISPATCHED)
        val order2 = Order.of("1234567892", "Book Name 2", BigDecimal.TWO, 5, OrderStatus.ACCEPTED)
        val expectedOrders = listOf(order1, order2)

        given { orderRepository.findAll() } returns Flux.fromIterable(expectedOrders)

        // When
        val result = orderService.getAllOrders()

        // Then
        StepVerifier.create(result).expectNextSequence(expectedOrders).verifyComplete()
        verify(exactly = 1) { orderRepository.findAll() }
    }

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

    @Test
    fun `when submitting an order and catalog request times out, then saves rejected order`() {
        // Given
        val isbn = "1234567890"
        val quantity = 3
        val timoutException =
            WebClientRequestException(
                ReadTimeoutException.INSTANCE,
                HttpMethod.GET,
                URI.create("http://localhost:9001/books/$isbn"),
                HttpHeaders(),
            )
        val orderToSave = Order.of(bookIsbn = isbn, quantity = quantity, status = OrderStatus.REJECTED)
        val savedOrder = orderToSave.copy(id = 1)

        given { bookClient.getBookByIsbn(isbn) } returns Mono.error(timoutException)
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
    fun `when submitting an order and catalog request fails for non-timeout reason, then propagates the error`() {
        // Given
        val isbn = "1234567890"
        val quantity = 3
        val connectionException = ConnectException("Catalog service is unavailable")
        val requestException =
            WebClientRequestException(
                connectionException,
                HttpMethod.GET,
                URI.create("http://localhost:9001/books/$isbn"),
                HttpHeaders(),
            )

        given { bookClient.getBookByIsbn(isbn) } returns Mono.error(requestException)

        // When
        val result = orderService.submitOrder(isbn, quantity)

        // Then
        StepVerifier
            .create(result)
            .expectErrorMatches { ex -> ex is WebClientRequestException && ex.cause is ConnectException }
            .verify()

        verify(exactly = 1) { bookClient.getBookByIsbn(isbn) }
        verify(exactly = 0) { orderRepository.save(any<Order>()) }
    }
}
