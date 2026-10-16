package com.polarbookshop.orderservice.book

import com.polarbookshop.orderservice.config.BookClient
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.junit5.StartStop
import okhttp3.Headers
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.test.StepVerifier
import kotlin.test.Test

class BookClientTest {
    @field:StartStop
    private val mockWebServer: MockWebServer = MockWebServer()

    private val bookClient by lazy {
        val webClient = WebClient.create(mockWebServer.url("/").toString())
        val adapter = WebClientAdapter.create(webClient)

        return@lazy HttpServiceProxyFactory.builderFor(adapter).build().createClient(BookClient::class.java)
    }

    @Test
    fun `when book exists then return book`() {
        val isbn = "1234567890"
        val headers =
            Headers.headersOf(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE,
            )
        val body =
            """
            {
              "isbn": "$isbn",
              "title": "Title",
              "author": "Author",
              "price": 9.90,
              "publisher": "Polarsophia"
            }
            """.trimIndent()
        val mockResponse =
            MockResponse()
                .newBuilder()
                .headers(headers)
                .body(body)
                .build()

        mockWebServer.enqueue(mockResponse)

        val book = bookClient.getBookByIsbn(isbn)

        StepVerifier.create(book).expectNextMatches { b -> b.isbn == isbn }.verifyComplete()
    }
}
