package com.polarbookshop.edgeservice.circuitbreaker

import com.polarbookshop.edgeservice.config.ServerMockConfiguration
import com.polarbookshop.edgeservice.config.TestcontainersConfiguration
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import mockwebserver3.SocketEffect
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
    private val catalogBreaker = circuitBreakerRegistry.circuitBreaker("catalogCircuitBreaker")

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
        circuitBreakerRegistry.circuitBreaker("orderCircuitBreaker").reset()
        mockCatalogServer.dispatcher = QueueDispatcher()
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
        delayMilliseconds: Long = 450,
        burstCount: Int = 4,
    ) {
        val delayedResponse =
            MockResponse
                .Builder()
                .code(HttpStatus.OK.value())
                .headersDelay(delayMilliseconds, TimeUnit.MILLISECONDS)
                .body("""[{"isbn":"1234567890","title":"Slow Catalog"}]""")
                .build()

        repeat(burstCount) { mockCatalogServer.enqueue(delayedResponse) }
    }

    @Nested
    inner class GivenCatalogBrowsing {
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

        @Test
        fun `when catalog-service exceeds timeout, then returns empty list with CATALOG_TIMEOUT header`() {
            // Given
            val expectedFallbackReason = "CATALOG_TIMEOUT"
            val mockSlowResponse =
                MockResponse
                    .Builder()
                    .headersDelay(6, TimeUnit.SECONDS)
                    .code(HttpStatus.OK.value())
                    .body("[]")
                    .build()
            repeat(4) { mockCatalogServer.enqueue(mockSlowResponse) }

            // When
            val response = webClient.get().uri("/books").exchange()

            // Then
            response
                .expectStatus()
                .isOk
                .expectHeader()
                .valueEquals("X-Fallback-Reason", expectedFallbackReason)
        }

        @Test
        fun `when catalog-service circuit is forced open, then short circuit immediately with CIRCUIT_OPEN reason`() {
            // Given
            val expectedFallbackReason = "CIRCUIT_OPEN"
            catalogBreaker.transitionToOpenState()

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

        @Test
        fun `when catalog-service closes connection, then return empty list with 200 OK`() {
            // Given
            val expectedFallbackReason = "CATALOG_TIMEOUT"
            slowCatalogResponse(delayMilliseconds = 450)

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

    @Nested
    inner class GivenOrderPlacement {
        private fun placeOrder(
            isbn: String = "1987654321",
            quantity: Int = 1,
        ) = webClient
            .post()
            .uri("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"isbn":$isbn,"quantity":$quantity}""")
            .exchange()

        @Test
        fun `when order-service fails, then propagate 5xx without silent fallback`() {
            // Given
            mockOrderServer.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest) = MockResponse(code = HttpStatus.SERVICE_UNAVAILABLE.value())
                }

            // When
            val response = placeOrder()

            // Then
            response.expectStatus().is5xxServerError
        }
    }
}
