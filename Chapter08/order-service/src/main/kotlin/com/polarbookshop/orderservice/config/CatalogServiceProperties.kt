package com.polarbookshop.orderservice.config

import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ConfigurationProperties(prefix = "polar.catalog-service")
class CatalogServiceProperties {
    @NotNull
    lateinit var url: URI
    var connectTimeout: Duration = 15.seconds.toJavaDuration()
    var responseTimeout: Duration = 30.seconds.toJavaDuration()
}
