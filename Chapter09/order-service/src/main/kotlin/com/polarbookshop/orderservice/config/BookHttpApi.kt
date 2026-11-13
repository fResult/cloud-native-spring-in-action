package com.polarbookshop.orderservice.config

import com.polarbookshop.orderservice.book.Book
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@HttpExchange("/books")
internal interface BookHttpApi {
    @GetExchange
    fun getBooks(): Flux<Book>

    @GetExchange("/{isbn}")
    fun getBookByIsbn(
        @PathVariable isbn: String,
    ): Mono<Book>
}
