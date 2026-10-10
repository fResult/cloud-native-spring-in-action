package com.polarbookshop.orderservice.order.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import kotlin.time.Instant

@Table("orders")
data class Order(
    @Id val id: Long? = null,
    val bookIsbn: String,
    val bookName: String,
    val bookPrice: BigDecimal,
    val quantity: Int,
    val status: OrderStatus,
    @CreatedDate val createdDate: Instant? = null,
    @LastModifiedDate val lastModifiedDate: Instant? = null,
    @Version val version: Int,
) {
    companion object {
        fun of(
            bookIsbn: String,
            bookName: String,
            bookPrice: BigDecimal,
            quantity: Int,
            status: OrderStatus,
        ): Order = Order(bookIsbn = bookIsbn, bookName = bookName, bookPrice = bookPrice, quantity = quantity, status = status, version = 0)
    }
}
