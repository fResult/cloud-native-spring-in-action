package com.polarbookshop.orderservice.config

import com.polarbookshop.orderservice.config.annotation.RequestTimeout
import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.netty.http.client.HttpClient
import java.lang.reflect.Method
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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
        catalogServiceProperties: CatalogServiceProperties,
        baseHttpClient: HttpClient,
        timeoutFilter: ExchangeFilterFunction,
        webClientBuilder: WebClient.Builder,
    ): WebClient =
        webClientBuilder
            .baseUrl(catalogServiceProperties.url.toString())
            .clientConnector(ReactorClientHttpConnector(baseHttpClient))
            .filter(timeoutFilter)
            .build()

    @Bean
    fun baseHttpClient(properties: CatalogServiceProperties): HttpClient =
        HttpClient
            .create()
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                properties.connectTimeout.toMillis().toInt(),
            ).responseTimeout(properties.responseTimeout)

    @Bean
    fun timeoutFilter(baseHttpClient: HttpClient): ExchangeFilterFunction =
        ExchangeFilterFunction { request, next ->
            val method =
                request
                    .attribute("org.springframework.web.service.invoker.HttpServiceArgumentResolver.CONTEXT")
                    .map { it as? Method }
                    .orElse(null)
            val annotation =
                method?.getAnnotation(RequestTimeout::class.java)
                    ?: return@ExchangeFilterFunction next.exchange(request)

            val timeoutSeconds = annotation.seconds.seconds.toJavaDuration()
            val timeBoundedRequest =
                ClientRequest
                    .from(request)
                    .attribute(HttpClient::class.jvmName, baseHttpClient.responseTimeout(timeoutSeconds))
                    .build()

            next.exchange(timeBoundedRequest)
        }
}
