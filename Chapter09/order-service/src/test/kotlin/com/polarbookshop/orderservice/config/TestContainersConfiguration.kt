package com.polarbookshop.orderservice.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestContainersConfiguration {
    @Bean
    @ServiceConnection
    fun postgreSQLContainer() = PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
}
