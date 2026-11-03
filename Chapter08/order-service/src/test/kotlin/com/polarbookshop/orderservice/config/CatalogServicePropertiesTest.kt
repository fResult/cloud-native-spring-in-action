package com.polarbookshop.orderservice.config

import org.junit.jupiter.api.Nested
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class CatalogServicePropertiesTest {
    private val catalogPropertiesContext = ApplicationContextRunner().withUserConfiguration(TestConfiguration::class.java)

    @EnableConfigurationProperties(CatalogServiceProperties::class)
    open class TestConfiguration

    @Nested
    inner class GivenValidCatalogServiceProperties {
        @Test
        fun `when correct URI and timeouts are provided, then application context loads properly`() {
            // Given
            val uri = "http://catalog-service"
            val timeout = 3.seconds


            // When & Then
            catalogPropertiesContext
                .withPropertyValues(
                    "polar.catalog-service.uri=$uri",
                    "polar.catalog-service.connect-timeout=$timeout",
                    "polar.catalog-service.response-timeout=$timeout",
                ).run { context ->
                    val properties = context.getBean(CatalogServiceProperties::class.java)

                    // Then
                    assertEquals(URI.create(uri), properties.uri)
                    assertEquals(timeout.toJavaDuration(), properties.connectTimeout)
                    assertEquals(timeout.toJavaDuration(), properties.responseTimeout)
                }
        }
    }

    @Nested
    inner class GivenInvalidCatalogServiceProperties {
        @Test
        fun `when key is misspelled, then fail at startup`() {
            // Given
            val misspelledKey = "polar.catalog-service.url=http://corrupted-uri:9999"

            // When & Then
            catalogPropertiesContext
                .withPropertyValues(misspelledKey)
                .run { context ->
                    // Then
                    assertNotNull(context.startupFailure)
                }
        }

        @Test
        fun `when unexpected or misspelled keys are provided, then they are ignored and only valid keys are mapped`() {
            // Given
            val validUri = "http://localhost:9001"
            val misspelledKey = "polar.catalog-service.url=http://corrupted-uri:9999"

            // When & Then
            catalogPropertiesContext
                .withPropertyValues(
                    "polar.catalog-service.uri=$validUri",
                    misspelledKey,
                ).run { context ->
                    // Then
                    assertNull(context.startupFailure)

                    val properties = context.getBean(CatalogServiceProperties::class.java)
                    assertEquals(URI.create(validUri), properties.uri)
                }
        }
    }
}
