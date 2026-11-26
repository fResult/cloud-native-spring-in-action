package com.polarbookshop.edgeservice.config

import mockwebserver3.MockWebServer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar

@TestConfiguration(proxyBeanMethods = false)
class ServerMockConfiguration {
    @Bean(destroyMethod = "close")
    fun mockCatalogServer() = MockWebServer().apply { start() }

    @Bean(destroyMethod = "close")
    fun mockOrderServer() = MockWebServer().apply { start() }

    @Bean
    fun registerServiceUrl(
        mockCatalogServer: MockWebServer,
        mockOrderServer: MockWebServer,
    ) = DynamicPropertyRegistrar { registry ->
        registry.add("CATALOG_SERVICE_URI") { mockCatalogServer.url("/books").toString().removeSuffix("/books/") }
        registry.add("ORDER_SERVICE_URL") { mockOrderServer.url("/orders").toString().removeSuffix("/orders/") }
    }
}
