package com.polarbookshop.edgeservice.config

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Headers.Companion.headersOf
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistrar

@TestConfiguration(proxyBeanMethods = false)
class GatewayMockConfiguration {
    @Bean
    fun mockWebServer() =
        MockWebServer()
            .apply {
                dispatcher =
                    object : Dispatcher() {
                        override fun dispatch(request: RecordedRequest) =
                            MockResponse
                                .Builder()
                                .code(HttpStatus.OK.value())
                                .headers(headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                                .body("[]")
                                .build()
                    }
                start()
            }

    @Bean
    fun dynamicProperties(mockWebServer: MockWebServer) =
        DynamicPropertyRegistrar { registry ->
            registry.add("CATALOG_SERVICE_URI") { mockWebServer.url("/").toString() }

            registry.add("RATE_LIMITER_REPLENISH_RATE") { 1 }
            registry.add("RATE_LIMITER_BURST_CAPACITY") { 2 }
        }
}
