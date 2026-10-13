package com.polarbookshop.orderservice.book

import java.math.BigDecimal

data class Book(
    val isbn: String,
    val title: String,
    val author: String,
    val price: BigDecimal,
)
