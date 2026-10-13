package com.polarbookshop.orderservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class ClientConfiguration {
    @Bean
    fun bookClient(proxyFactory: HttpServiceProxyFactory): BookClient = proxyFactory.createClient(BookClient::class.java)

    @Bean
    fun proxyFactory(webClient: WebClient): HttpServiceProxyFactory {
        val adapter = WebClientAdapter.create(webClient)
        return HttpServiceProxyFactory.builderFor(adapter).build()
    }

    @Bean
    fun webClient(
        polarProperties: PolarProperties,
        webClientBuilder: WebClient.Builder,
    ): WebClient =
        webClientBuilder
            .baseUrl(polarProperties.catalogServiceUrl.toString())
            .build()
}
