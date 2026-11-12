package com.polarbookshop.orderservice.book.web

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookHttpApi
import com.polarbookshop.orderservice.config.CatalogServiceProperties
import org.springframework.core.NestedExceptionUtils
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorResume
import reactor.util.retry.Retry
import java.net.ConnectException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@Component
class CatalogBookClient internal constructor(
    private val httpApi: BookHttpApi,
    private val properties: CatalogServiceProperties,
) {
    private val transientRetry = Retry.backoff(3, 100.milliseconds.toJavaDuration()).filter(::isTransientFailure)

    fun getBooks(): Flux<Book> = httpApi.getBooks()

    fun getBookByIsbn(isbn: String): Mono<Book> =
        httpApi
            .getBookByIsbn(isbn)
            .timeout(properties.responseTimeout, Mono.empty())
            .onErrorResume(WebClientResponseException.NotFound::class) { _ -> Mono.empty() }
            .retryWhen(transientRetry)
            .onErrorResume(Exception::class) { _ -> Mono.empty() }

    private fun isTransientFailure(ex: Throwable) =
        when (ex) {
            is WebClientRequestException -> NestedExceptionUtils.getMostSpecificCause(ex) is ConnectException
            is WebClientResponseException -> ex.statusCode.is5xxServerError
            else -> false
        }
}
