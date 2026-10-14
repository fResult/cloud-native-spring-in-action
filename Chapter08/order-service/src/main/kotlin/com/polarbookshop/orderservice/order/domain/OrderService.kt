package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookClient
import io.netty.handler.timeout.ReadTimeoutException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorResume

@Service
class OrderService(
    private val bookClient: BookClient,
    private val orderRepository: OrderRepository,
) {
    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

    fun submitOrder(
        isbn: String,
        quantity: Int,
    ): Mono<Order> =
        bookClient
            .getBookByIsbn(isbn)
            .map { book -> buildAcceptedOrder(book, quantity) }
            .onErrorResume(WebClientResponseException.NotFound::class) { Mono.just(buildRejectedOrder(isbn, quantity)) }
            .onErrorResume(WebClientRequestException::class, rejectWhenCatalogReadTimeoutFor(isbn, quantity))
            .flatMap(orderRepository::save)

    private fun buildAcceptedOrder(
        book: Book,
        quantity: Int,
    ): Order = Order.of(book.isbn, "${book.title} - ${book.author}", book.price, quantity, OrderStatus.ACCEPTED)

    private fun rejectWhenCatalogReadTimeoutFor(
        isbn: String,
        quantity: Int,
    ): (WebClientRequestException) -> Mono<Order> =
        { ex ->
            when (ex.cause) {
                is ReadTimeoutException -> Mono.just(buildRejectedOrder(isbn, quantity))
                else -> Mono.error(ex)
            }
        }

    private fun buildRejectedOrder(
        isbn: String,
        quantity: Int,
    ): Order = Order.of(bookIsbn = isbn, quantity = quantity, status = OrderStatus.REJECTED)
}
