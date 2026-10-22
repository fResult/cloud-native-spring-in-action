package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.config.BookClient
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @MockK
    private lateinit var bookClient: BookClient

    @MockK
    private lateinit var orderRepository: OrderRepository

    @InjectMockKs
    private lateinit var orderService: OrderService
}
