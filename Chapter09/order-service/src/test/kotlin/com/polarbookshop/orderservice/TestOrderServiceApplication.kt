package com.polarbookshop.orderservice

import com.polarbookshop.orderservice.config.TestContainersConfiguration
import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<OrderServiceApplication>().with(TestContainersConfiguration::class).run(*args)
}
