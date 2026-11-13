package com.polarbookshop.orderservice.book.web.fixture

import org.springframework.http.HttpStatus

enum class NonRetryableCatalogResponseTestCase(
    private val description: String,
    val status: HttpStatus,
) {
    BAD_REQUEST("Catalog returns 400 Bad Request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("Catalog returns 401 Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Catalog returns 403 Forbidden", HttpStatus.FORBIDDEN),
    CONFLICT("Catalog returns 409 Conflict", HttpStatus.CONFLICT),
    ;

    override fun toString() = description
}
