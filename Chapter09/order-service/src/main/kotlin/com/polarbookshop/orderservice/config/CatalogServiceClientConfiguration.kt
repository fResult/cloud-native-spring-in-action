package com.polarbookshop.orderservice.config

import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.netty.http.client.HttpClient

@Configuration
class CatalogServiceClientConfiguration {
    @Bean
    internal fun bookHttpApi(catalogServiceWebClient: WebClient): BookHttpApi {
        val adapter = WebClientAdapter.create(catalogServiceWebClient)
        val proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build()

        return proxyFactory.createClient(BookHttpApi::class.java)
    }

    @Bean
    fun catalogServiceWebClient(
        catalogServiceProperties: CatalogServiceProperties,
        catalogServiceHttpClient: HttpClient,
        webClientBuilder: WebClient.Builder,
    ): WebClient =
        webClientBuilder
            .baseUrl(catalogServiceProperties.uri.toString())
            .clientConnector(ReactorClientHttpConnector(catalogServiceHttpClient))
            .build()

    @Bean
    fun catalogServiceHttpClient(properties: CatalogServiceProperties): HttpClient =
        HttpClient
            .create()
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                properties.connectTimeout.toMillis().toInt(),
            ).responseTimeout(properties.responseTimeout)
}
