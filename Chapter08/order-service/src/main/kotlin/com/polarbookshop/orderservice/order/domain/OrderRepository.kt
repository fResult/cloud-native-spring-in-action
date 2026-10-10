package com.polarbookshop.orderservice.order.domain

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OrderRepository : ReactiveCrudRepository<Order, Long>
