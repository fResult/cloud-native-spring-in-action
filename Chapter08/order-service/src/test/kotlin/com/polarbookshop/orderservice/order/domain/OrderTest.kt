package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.Book
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderTest {
    @Test
    fun `when building an accepted order then success`() {
        // Given
        val isbn = "1234567890"
        val title = "Title"
        val author = "Author"
        val bookName = listOf(title, author).joinToString(" - ")
        val price = BigDecimal.valueOf(9.90)
        val book = Book(isbn, title, author, price)
        val expectedOrder = Order.of(isbn, bookName, price, 3, OrderStatus.ACCEPTED)

        // When
        val actualOrder = Order.buildAcceptedOrder(book, 3)

        // Then
        assertEquals(expectedOrder, actualOrder)
    }
}
