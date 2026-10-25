package com.polarbookshop.orderservice.book.web

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookHttpApi
import io.netty.handler.timeout.TimeoutException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorResume
import reactor.util.retry.Retry
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Component
class CatalogBookClient internal constructor(
    private val httpApi: BookHttpApi,
) {
    fun getBooks(): Flux<Book> = httpApi.getBooks()

    fun getBookByIsbn(isbn: String): Mono<Book> =
        httpApi
            .getBookByIsbn(isbn)
            .timeout(3.seconds.toJavaDuration(), Mono.empty())
            .onErrorResume(WebClientResponseException.NotFound::class) { _ -> Mono.empty() }
            .retryWhen(Retry.backoff(3, 100.milliseconds.toJavaDuration()).filter(::isRetryable))
            .onErrorResume(Exception::class) { _ -> Mono.empty() }

    fun isRetryable(ex: Throwable) =
        ex !is WebClientResponseException.NotFound &&
            ex !is TimeoutException
}
