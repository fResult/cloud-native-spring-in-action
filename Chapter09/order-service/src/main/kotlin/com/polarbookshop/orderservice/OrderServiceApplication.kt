package com.polarbookshop.orderservice

import com.polarbookshop.orderservice.config.CatalogServiceProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication
@EnableConfigurationProperties(CatalogServiceProperties::class)
class OrderServiceApplication

fun main(args: Array<String>) {
    ReactorDebugAgent.init()
    // ReactorDebugAgent.processExistingClasses()
    runApplication<OrderServiceApplication>(*args)
}
