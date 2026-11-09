package com.polarbookshop.orderservice.order.web.fixture

import com.polarbookshop.orderservice.order.web.OrderRequest

data class InvalidOrderRequestTestCase(
    private val description: String,
    val request: OrderRequest,
    val expectedFieldErrors: Map<String, String>,
) {
    override fun toString() = description
}
