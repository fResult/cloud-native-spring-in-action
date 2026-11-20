package com.polarbookshop.edgeservice.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class WebEndpoints {
    @Bean
    fun routers() =
        coRouter {
            GET("/catalog-fallback") { _ -> ServerResponse.ok().bodyValueAndAwait("FALLBACK") }
            POST("/catalog-fallback") { _ -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).buildAndAwait() }
        }
}
