package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.config.BookClient
import io.netty.handler.timeout.ReadTimeoutException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorResume

@Service
class OrderService(
    private val bookClient: BookClient,
    private val orderRepository: OrderRepository,
) {
    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

    fun submitOrder(
        isbn: String,
        quantity: Int,
    ): Mono<Order> =
        bookClient
            .getBookByIsbn(isbn)
            .map { book -> Order.buildAcceptedOrder(book, quantity) }
            .onErrorResume(WebClientResponseException.NotFound::class, rejectWhenBookIsNotFoundFor(isbn, quantity))
            .onErrorResume(WebClientRequestException::class, rejectWhenCatalogReadTimeoutFor(isbn, quantity))
            .flatMap(orderRepository::save)

    private fun rejectWhenBookIsNotFoundFor(
        isbn: String,
        quantity: Int,
    ): (WebClientResponseException.NotFound) -> Mono<Order> = { _ -> Mono.just(Order.buildRejectedOrder(isbn, quantity)) }

    private fun rejectWhenCatalogReadTimeoutFor(
        isbn: String,
        quantity: Int,
    ): (WebClientRequestException) -> Mono<Order> =
        { ex ->
            when (ex.cause) {
                is ReadTimeoutException -> Mono.just(Order.buildRejectedOrder(isbn, quantity))
                else -> Mono.error(ex)
            }
        }
}
