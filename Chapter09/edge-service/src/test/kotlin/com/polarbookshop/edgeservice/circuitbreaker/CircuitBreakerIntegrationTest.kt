package com.polarbookshop.edgeservice.circuitbreaker

import com.polarbookshop.edgeservice.config.ServerMockConfiguration
import com.polarbookshop.edgeservice.config.TestcontainersConfiguration
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.QueueDispatcher
import mockwebserver3.SocketEffect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class, ServerMockConfiguration::class)
class CircuitBreakerIntegrationTest(
    @param:Autowired
    private val mockCatalogServer: MockWebServer,
    @param:Autowired
    private val mockOrderServer: MockWebServer,
    @param:Autowired
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    @param:LocalServerPort
    private val port: Int = 0,
) {
    private val webClient: WebTestClient by lazy {
        WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$port")
            .responseTimeout(15.seconds.toJavaDuration())
            .build()
    }

    @BeforeTest
    fun setUp() {
        circuitBreakerRegistry.circuitBreaker("catalogCircuitBreaker").reset()
        mockOrderServer.dispatcher = QueueDispatcher()
    }

    private fun midCatalogRepsonseSocketDrop(burstCount: Int = 4) {
        val dropMidResponse =
            MockResponse
                .Builder()
                .code(HttpStatus.OK.value())
                .body("""[{"isbn":"1234567890","title":"Domain-Driven Design"}]""")
                .onResponseStart(SocketEffect.CloseSocket())
                .build()

        repeat(burstCount) { mockCatalogServer.enqueue(dropMidResponse) }
    }

    private fun slowCatalogResponse(
        delaySeconds: Long = 6,
        burstCount: Int = 4,
    ) {
        val delayedResponse =
            MockResponse
                .Builder()
                .code(HttpStatus.OK.value())
                .headersDelay(delaySeconds, TimeUnit.SECONDS)
                .body("""[{"isbn":"1234567890","title":"Slow Catalog"}]""")
                .build()

        repeat(burstCount) { mockCatalogServer.enqueue(delayedResponse) }
    }

    @Nested
    inner class GivenCatalogServiceWithFallback {
        @Test
        fun `when catalog-service drops connection mid response, then return empty list with SERVICE_UNAVAILABLE reason`() {
            // Given
            val expectedFallbackReason = "SERVICE_UNAVAILABLE"
            midCatalogRepsonseSocketDrop()

            // When
            val response = webClient.get().uri("/books").exchange()

            // Then
            response
                .expectStatus()
                .isOk
                .expectHeader()
                .valueEquals("X-Fallback-Reason", expectedFallbackReason)
                .expectBodyList<Any>()
                .hasSize(0)
        }
    }
}
