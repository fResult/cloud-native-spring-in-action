package com.polarbookshop.edgeservice.config

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@Tag("integration")
@Import(TestcontainersConfiguration::class, GatewayMockConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitingIntegrationTest(
    @param:Autowired
    private val redisTemplate: ReactiveStringRedisTemplate,
    @param:LocalServerPort
    private val port: Int = 0,
) {
    private val webClient: WebTestClient by lazy {
        WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @BeforeTest
    fun resetRateLimiterTokens() {
        redisTemplate.execute { connection -> connection.serverCommands().flushAll() }.blockLast()
    }

    @Nested
    inner class GivenTrafficWithinBurstCapacity {
        @Test
        fun `when requests do not exceed burst capacity, then accepts all traffic with 200 OK`() {
            // Given
            val burstCapacity = 2
            val rateLimitRemainingHeader = "X-RateLimit-Remaining"

            repeat(burstCapacity) {
                // When
                val response = webClient.get().uri("/books").exchange()

                // Then
                response
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .exists(rateLimitRemainingHeader)
            }
        }
    }

    @Nested
    inner class GivenTrafficExceedingBurstCapacity {
        @Test
        fun `when requests exceed burst limit, then throttles excess traffic with 429 Too Many Requests`() {
            // Given
            val expectedStatus = HttpStatus.TOO_MANY_REQUESTS
            val expectedRateLimitingRemaining = "0"

            val burstCapacity = 2
            val targetUri = "/books"
            val rateLimitRemainingHeader = "X-RateLimit-Remaining"

            repeat(burstCapacity) {
                webClient
                    .get()
                    .uri(targetUri)
                    .exchange()
                    .expectStatus()
                    .isOk
            }

            // When
            val excessRequestResponse = webClient.get().uri(targetUri).exchange()

            // Then
            excessRequestResponse
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectHeader()
                .valueEquals(rateLimitRemainingHeader, expectedRateLimitingRemaining)
        }

        @Test
        fun `when throttled client waits for token replenish, then accepts subsequent requests`() {
            // Given
            val expectedRemainingTokensAfterReplenish = setOf(0, 1)

            val burstCapacity = 2
            val targetUri = "/books"
            val rateLimitRemainingHeader = "X-RateLimit-Remaining"

            repeat(burstCapacity) {
                webClient
                    .get()
                    .uri(targetUri)
                    .exchange()
                    .expectStatus()
                    .isOk
            }

            // When
            Thread.sleep(1000)
            val subsequentRequestResponse = webClient.get().uri(targetUri).exchange()

            // Then
            subsequentRequestResponse
                .expectStatus()
                .isOk
                .expectHeader()
                .exists(rateLimitRemainingHeader)
                .expectHeader()
                .value(rateLimitRemainingHeader, assertWithin(expectedRemainingTokensAfterReplenish))
        }

        private fun assertWithin(expectedTokens: Set<Int>): (String) -> Unit =
            { remainingTokens ->
                val actualRemainingTokens = remainingTokens.toInt()
                val acceptableTokenRange = expectedTokens.joinToString(" and ")

                assertTrue(
                    actualRemainingTokens in expectedTokens,
                    """
                    Expected remaining tokens to be one of $expectedTokens, but was $actualRemainingTokens.
                    Remaining tokens may vary between $acceptableTokenRange due to execution time drift against Redis epoch seconds.
                    """.trimIndent(),
                )
            }
    }
}
