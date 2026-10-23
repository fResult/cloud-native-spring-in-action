package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookClient
import io.mockk.bdd.given
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
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
}
