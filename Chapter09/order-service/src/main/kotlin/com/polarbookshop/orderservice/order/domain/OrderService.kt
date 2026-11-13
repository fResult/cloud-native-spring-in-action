package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.web.CatalogBookClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class OrderService(
    private val bookClient: CatalogBookClient,
    private val orderRepository: OrderRepository,
) {
    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

    fun submitOrder(
        isbn: String,
        quantity: Int,
    ): Mono<Order> =
        bookClient
            .getBookByIsbn(isbn)
            .map { book -> Order.buildAcceptedOrder(book, quantity) }
            .switchIfEmpty { Mono.just(Order.buildRejectedOrder(isbn, quantity)) }
            .flatMap(orderRepository::save)
}
