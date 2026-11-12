package com.polarbookshop.orderservice.book.web.fixture

import org.springframework.http.HttpStatus

data class NonRetryableCatalogResponseTestCase(
    private val description: String,
    val status: HttpStatus,
) {
    override fun toString() = description
}
