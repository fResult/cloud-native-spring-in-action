package com.polarbookshop.edgeservice.web

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.net.ConnectException
import java.time.Instant
import java.util.concurrent.TimeoutException

@Configuration
class WebEndpoints {
    @Bean
    fun routers() =
        coRouter {
            GET("/catalog-fallback", handleFallback(isReadOperation = true))
            POST("/catalog-fallback", handleFallback(isReadOperation = false))
        }

    private fun handleFallback(isReadOperation: Boolean): suspend (ServerRequest) -> ServerResponse =
        { request ->
            val httpStatus = HttpStatus.SERVICE_UNAVAILABLE
            val exception = request.exchange().getAttribute<Throwable>(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR)

            if (isReadOperation) {
                ServerResponse
                    .ok()
                    .header("X-Fallback-Reason", toReason(exception))
                    .bodyValueAndAwait(emptyList<Any>())
            } else {
                val detail =
                    ProblemDetail
                        .forStatusAndDetail(httpStatus, "Catalog Service is temporarily unavailable. Please try again later.")
                        .apply {
                            type = request.uri()
                            setProperty("timestamp", Instant.now())
                            setProperty("cause", toReason(exception))
                        }

                ServerResponse
                    .status(httpStatus)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyValueAndAwait(detail)
            }
        }

    private fun toReason(exception: Throwable?) =
        when (exception) {
            is CallNotPermittedException -> "CIRCUIT_OPEN"
            is TimeoutException -> "CATALOG_TIMEOUT"
            is ConnectException -> "CONNECTION_REFUSED"
            else -> "SERVICE_UNAVAILABLE"
        }
}
