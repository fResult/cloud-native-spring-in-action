package com.polarbookshop.orderservice

import com.polarbookshop.orderservice.config.TestContainersConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.blockhound.BlockHound

@Testcontainers
@Import(TestContainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            BlockHound.install()
        }
    }

    @Test
    fun contextLoads() {
    }
}
