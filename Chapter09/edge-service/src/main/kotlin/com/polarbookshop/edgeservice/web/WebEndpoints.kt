package com.polarbookshop.edgeservice.web

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import java.time.Instant

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
                    .header(
                        "X-Fallback-Reason",
                        exception?.javaClass?.simpleName ?: httpStatus.name,
                    ).bodyValueAndAwait("")
            }

            val detail =
                ProblemDetail
                    .forStatusAndDetail(httpStatus, "Catalog Service is temporarily unavailable. Please try again later.")
                    .apply {
                        type = request.uri()
                        setProperty("timestamp", Instant.now())
                        setProperty("cause", exception?.javaClass?.simpleName ?: "Unknown")
                    }

            ServerResponse.status(httpStatus).bodyValueAndAwait(detail)
        }
}
