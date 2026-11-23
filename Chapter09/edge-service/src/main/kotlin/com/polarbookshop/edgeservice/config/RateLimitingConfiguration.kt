package com.polarbookshop.edgeservice.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class RateLimitingConfiguration {
    @Bean
    fun keyResolver(): KeyResolver = { _ -> Mono.just("anonymous") }
}
