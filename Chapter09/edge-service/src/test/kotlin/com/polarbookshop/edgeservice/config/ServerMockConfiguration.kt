package com.polarbookshop.edgeservice.config

import com.polarbookshop.edgeservice.testsupport.http.httpUri
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
        registry.add("CATALOG_SERVICE_URI", mockCatalogServer::httpUri)
        registry.add("ORDER_SERVICE_URI", mockOrderServer::httpUri)
    }
}
