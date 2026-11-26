package com.polarbookshop.edgeservice.circuitbreaker

import com.polarbookshop.edgeservice.config.ServerMockConfiguration
import com.polarbookshop.edgeservice.config.TestcontainersConfiguration
import mockwebserver3.MockWebServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class, ServerMockConfiguration::class)
class CircuitBreakerIntegrationTest(
    @param:Autowired
    private val mockCatalogServer: MockWebServer,
    @param:Autowired
    private val mockOrderServer: MockWebServer,
    @param:LocalServerPort
    private val port: Int = 0,
) {
    private val webClient: WebTestClient by lazy {
        WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }
}
