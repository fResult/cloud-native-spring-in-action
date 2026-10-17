package com.polarbookshop.orderservice.book

import com.polarbookshop.orderservice.config.BookClient
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.junit5.StartStop
import okhttp3.Headers.Companion.headersOf
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import kotlin.test.Test

class BookClientTest {
    @field:StartStop
    private val mockWebServer: MockWebServer = MockWebServer()

    val mapper = ObjectMapper()
    val responseWith = mockWebServer.responseWith(mapper)

    private val bookClient by lazy {
        val webClient = WebClient.create(mockWebServer.url("/").toString())
        val adapter = WebClientAdapter.create(webClient)

        return@lazy HttpServiceProxyFactory.builderFor(adapter).build().createClient(BookClient::class.java)
    }

    @Test
    fun `when book exists then return book`() {
        // Given
        val expectedBook = Book("1234567890", "Title", "Author", BigDecimal.valueOf(9.90))
        responseWith(expectedBook, HttpStatus.OK)

        // When
        val actualBook = bookClient.getBookByIsbn(expectedBook.isbn)

        // Then
        StepVerifier.create(actualBook).expectNext(expectedBook).verifyComplete()
    }

    @Test
    fun `when book doesn't exists then book not found`() {
        // Given
        val isbn = "not-found-isbn"
        val mockResponse = MockResponse(HttpStatus.NOT_FOUND.value())
        mockWebServer.enqueue(mockResponse)

        // When
        val notFoundBook = bookClient.getBookByIsbn(isbn)

        // Then
        StepVerifier.create(notFoundBook).expectError(WebClientResponseException::class.java).verify()
    }
}

private fun MockWebServer.responseWith(mapper: ObjectMapper): (Book, HttpStatus) -> Unit =
    { book, status ->
        val headers = headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        val mockResponse =
            MockResponse(code = status.value(), headers = headers, body = mapper.writeValueAsString(book))

        enqueue(mockResponse)
    }
