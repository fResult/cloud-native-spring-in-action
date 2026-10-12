package com.polarbookshop.orderservice.order.domain

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class OrderService(
    private val orderRepository: OrderRepository,
) {
    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

    fun submitOrder(
        isbn: String,
        quantity: Int,
    ): Mono<Order> = Mono.just(buildRejectedOrder(isbn, quantity)).flatMap(orderRepository::save)

    private fun buildRejectedOrder(
        isbn: String,
        quantity: Int,
    ): Order = Order.of(bookIsbn = isbn, quantity = quantity, status = OrderStatus.REJECTED)
}
