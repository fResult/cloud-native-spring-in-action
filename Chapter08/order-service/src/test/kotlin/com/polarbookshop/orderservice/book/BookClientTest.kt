package com.polarbookshop.orderservice.book

import com.polarbookshop.orderservice.config.BookClient
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.junit5.StartStop
import okhttp3.Headers.Companion.headersOf
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
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
import kotlin.test.assertEquals

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
    fun `when books exist then return books`() {
        val expectedBooks =
            listOf(
                Book("1234567891", "Title1", "Author1", BigDecimal.valueOf(19.90)),
                Book("1234567892", "Title2", "Author2", BigDecimal.valueOf(12.90)),
            )
        responseWith(expectedBooks)

        // WHen
        val actualBooks = bookClient.fetchBooks()

        // Then
        StepVerifier
            .create(actualBooks)
            .expectNext(expectedBooks.first())
            .expectNext(expectedBooks[1])
            .verifyComplete()

        val request = mockWebServer.takeRequest()
        assertEquals(HttpMethod.GET.name(), request.method)
        assertEquals("/books", request.target)
    }

    @Test
    fun `when no books exist then complete without emitting books`() {
        // Given
        responseWith(response = emptyList<Book>())

        // When
        val notFoundBook = bookClient.fetchBooks()

        // Then
        StepVerifier.create(notFoundBook).expectNextCount(0).verifyComplete()
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

        val request = mockWebServer.takeRequest()
        assertEquals(HttpMethod.GET.name(), request.method)
        assertEquals("/books/${expectedBook.isbn}", request.target)
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

interface MockResponseEnqueuer {
    operator fun <T> invoke(
        response: T? = null,
        status: HttpStatus = HttpStatus.OK,
    )
}

private fun MockWebServer.responseWith(mapper: ObjectMapper): MockResponseEnqueuer =
    object : MockResponseEnqueuer {
        override fun <T> invoke(
            response: T?,
            status: HttpStatus,
        ) {
            val headers = headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            val mockResponse =
                MockResponse(
                    code = status.value(),
                    headers = headers,
                    body = response.let { mapper.writeValueAsString(it) }.orEmpty(),
                )
            enqueue(mockResponse)
        }
    }
