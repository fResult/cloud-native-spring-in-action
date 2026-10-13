package com.polarbookshop.orderservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ClientConfiguration {
    @Bean
    fun webClient(
        polarProperties: PolarProperties,
        webClientBuilder: WebClient.Builder,
    ): WebClient =
        webClientBuilder
            .baseUrl(polarProperties.catalogServiceUrl.toString())
            .build()
}
