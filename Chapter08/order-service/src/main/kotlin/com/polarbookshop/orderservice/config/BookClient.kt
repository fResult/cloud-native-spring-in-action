package com.polarbookshop.orderservice.config

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.annotation.RequestTimeout
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@HttpExchange("/books")
interface BookClient {
    @GetExchange
    @RequestTimeout(3)
    fun fetchBooks(): Flux<Book>

    @RequestTimeout(3)
    @GetExchange("/{isbn}")
    fun getBookByIsbn(
        @PathVariable isbn: String,
    ): Mono<Book>
}
