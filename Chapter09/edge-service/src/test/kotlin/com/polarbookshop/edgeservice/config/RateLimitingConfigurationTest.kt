package com.polarbookshop.edgeservice.config

import org.junit.jupiter.api.Nested
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.test.StepVerifier
import kotlin.test.Test

class RateLimitingConfigurationTest {
    private val rateLimitingConfig = RateLimitingConfiguration()
    private val keyResolver = rateLimitingConfig.keyResolver()

    @Nested
    inner class GivenUnauthenticatedRequest {
        @Test
        fun `when resolves client identity, then identifies client as anonymous`() {
            // Given
            val expectedClientIdentity = "anonymous"
            val incomingRequest = requestTo("/books")

            // When
            val resolvedIdentity = keyResolver.resolve(incomingRequest)

            // Then
            StepVerifier.create(resolvedIdentity).expectNext(expectedClientIdentity).verifyComplete()
        }

        @Test
        fun `when resolving different paths, then consistently groups  under same anonymous identity`() {
            // Given
            val expectedClientIdentity = "anonymous"
            val booksRequest = requestTo("/books")
            val ordersRequest = requestTo("/orders")

            // When
            val resolvedBooksRequestIdentity = keyResolver.resolve(booksRequest)
            val resolvedOrdersIdentity = keyResolver.resolve(ordersRequest)

            // Then
            StepVerifier.create(resolvedBooksRequestIdentity).expectNext(expectedClientIdentity).verifyComplete()
            StepVerifier.create(resolvedOrdersIdentity).expectNext(expectedClientIdentity).verifyComplete()
        }
    }

    private fun requestTo(path: String) = MockServerWebExchange.from(MockServerHttpRequest.get(path))
}
