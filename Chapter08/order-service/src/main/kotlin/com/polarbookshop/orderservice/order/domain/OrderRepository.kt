package com.polarbookshop.orderservice.order.domain

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface OrderRepository : ReactiveCrudRepository<Order, Long> {
    fun findByStatus(status: OrderStatus): Flux<Order>
}
