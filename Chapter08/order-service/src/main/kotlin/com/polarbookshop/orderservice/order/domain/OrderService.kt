package com.polarbookshop.orderservice.order.domain

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class OrderService(
    private val orderRepository: OrderRepository,
) {
    fun getAllOrders(): Flux<Order> = orderRepository.findAll()
}
