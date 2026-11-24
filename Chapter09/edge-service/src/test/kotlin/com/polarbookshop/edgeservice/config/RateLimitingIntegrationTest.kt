package com.polarbookshop.edgeservice.config

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.BeforeTest
import kotlin.test.Test

@Tag("integration")
@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitingIntegrationTest {
    @LocalServerPort
    private val port = 0

    private val webClient: WebTestClient by lazy {
        WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    @BeforeTest
    fun resetRateLimiterTokens() {
        redisTemplate.execute { connection -> connection.serverCommands().flushAll() }.blockLast()
    }

    @Nested
    inner class GivenTrafficWithinBurstCapacity {
        @Test
        fun `when requests do not exceed client traffic within allowed capacity`() {
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
}
