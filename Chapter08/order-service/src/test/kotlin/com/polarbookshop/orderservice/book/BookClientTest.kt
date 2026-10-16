package com.polarbookshop.orderservice.book

import com.polarbookshop.orderservice.config.BookClient
import mockwebserver3.MockWebServer
import mockwebserver3.junit5.StartStop
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

class BookClientTest {
    @field:StartStop
    private val mockWebServer: MockWebServer = MockWebServer()

    private val bookClient by lazy {
        val webClient = WebClient.create(mockWebServer.url("/").toString())
        val adapter = WebClientAdapter.create(webClient)

        return@lazy HttpServiceProxyFactory.builderFor(adapter).build().createClient(BookClient::class.java)
    }
}
