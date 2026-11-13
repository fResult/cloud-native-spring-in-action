package com.polarbookshop.orderservice.order.web.fixture

import com.polarbookshop.orderservice.order.web.OrderRequest

data class ValidOrderRequestTestCase(
    private val description: String,
    val request: OrderRequest,
) {
    override fun toString() = description
}
