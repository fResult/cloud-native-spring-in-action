package com.polarbookshop.orderservice.config

import com.polarbookshop.orderservice.config.annotation.RequestTimeout
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutException
import io.netty.handler.timeout.WriteTimeoutException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.RetryBackoffSpec
import java.lang.reflect.Method
import java.net.ConnectException
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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
        catalogTimeoutFilter: ExchangeFilterFunction,
        catalogServiceRetryFilter: ExchangeFilterFunction,
        webClientBuilder: WebClient.Builder,
    ): WebClient =
        webClientBuilder
            .baseUrl(catalogServiceProperties.url.toString())
            .clientConnector(ReactorClientHttpConnector(catalogServiceHttpClient))
            .filter(catalogTimeoutFilter.andThen(catalogServiceRetryFilter))
            .build()

    @Bean
    fun catalogServiceHttpClient(properties: CatalogServiceProperties): HttpClient =
        HttpClient
            .create()
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                properties.connectTimeout.toMillis().toInt(),
            ).responseTimeout(properties.responseTimeout)

    @Bean
    fun catalogTimeoutFilter(baseHttpClient: HttpClient): ExchangeFilterFunction =
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

    @Bean
    fun catalogServiceRetryFilter(catalogServiceRetryPolicy: RetryBackoffSpec): ExchangeFilterFunction =
        ExchangeFilterFunction { request, next ->
            next
                .exchange(request)
                .flatMap(::failOnServerError)
                .retryWhen(catalogServiceRetryPolicy)
        }

    @Bean
    fun catalogServiceRetryPolicy(): RetryBackoffSpec =
        RetryBackoffSpec
            .backoff(3, 100.milliseconds.toJavaDuration())
            .filter(::isRecoverableError)

    private fun failOnServerError(response: ClientResponse): Mono<ClientResponse> =
        when (response.statusCode().is5xxServerError) {
            true -> response.createException().flatMap(Mono<ClientResponse>::error)
            false -> Mono.just(response)
        }

    private fun isRecoverableError(ex: Throwable): Boolean =
        ex is WebClientResponseException ||
            ex is ReadTimeoutException ||
            ex is WriteTimeoutException ||
            ex is ConnectException
}
