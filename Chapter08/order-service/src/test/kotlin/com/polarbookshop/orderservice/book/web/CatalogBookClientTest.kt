package com.polarbookshop.orderservice.book.web

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookHttpApi
import com.polarbookshop.orderservice.config.CatalogServiceProperties
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.junit5.StartStop
import okhttp3.Headers.Companion.headersOf
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

class CatalogBookClientTest {
    @field:StartStop
    private val mockWebServer: MockWebServer = MockWebServer()

    val mapper = ObjectMapper()
    val responseWith = mockWebServer.responseWith(mapper)

    private val bookHttpApi by lazy {
        val webClient = WebClient.create(mockWebServer.url("/").toString())
        val adapter = WebClientAdapter.create(webClient)

        return@lazy HttpServiceProxyFactory.builderFor(adapter).build().createClient(BookHttpApi::class.java)
    }

    private val bookClient by lazy {
        val properties =
            CatalogServiceProperties().apply {
                uri = URI.create("http://localhost:${mockWebServer.port}")
                responseTimeout = 200.milliseconds.toJavaDuration()
            }

        return@lazy CatalogBookClient(bookHttpApi, properties)
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
        val actualBooks = bookClient.getBooks()

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
        val notFoundBook = bookClient.getBooks()

        // Then
        StepVerifier.create(notFoundBook).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `when book exists, then return book`() {
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
    fun `when book doesn't exists, then returns no book`() {
        // Given
        val nonExistingBookIsbn = "not-found-isbn"
        val mockResponse = MockResponse(HttpStatus.NOT_FOUND.value())
        mockWebServer.enqueue(mockResponse)

        // When
        val notFoundBook = bookClient.getBookByIsbn(nonExistingBookIsbn)

        // Then
        StepVerifier.create(notFoundBook).expectNextCount(0).verifyComplete()
    }

    @Test
    fun `when book request times out, then returns no book`() {
        // Given
        val expectedNoRetryRequestCount = 1

        val isbn = "1234567890"
        val mockResposne =
            MockResponse
                .Builder()
                .body("""{"title": "Title"}""")
                .headers(headersOf("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .headersDelay(5, TimeUnit.SECONDS)
                .bodyDelay(5, TimeUnit.SECONDS)
                .build()
        mockWebServer.enqueue(mockResposne)

        // When
        val result = bookClient.getBookByIsbn(isbn)

        // Then
        StepVerifier
            .create(result)
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        assertEquals(expectedNoRetryRequestCount, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog returns 503 once, then retry and return book`() {
        // Given
        val book = Book("1234567890", "Title", "Author", BigDecimal.valueOf(9.90))

        mockWebServer.enqueue(MockResponse(HttpStatus.SERVICE_UNAVAILABLE.value()))
        responseWith(book)

        // When
        val result = bookClient.getBookByIsbn(book.isbn)

        // Then
        StepVerifier.create(result).expectNext(book).verifyComplete()
        assertEquals(2, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog does not respond before the timeout, then returns no book without retrying`() {
        // Given
        val expectedNoRetryRequestCount = 1

        val isbn = "1234567890"
        val slowResponse =
            MockResponse
                .Builder()
                .headers(headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .headersDelay(1, TimeUnit.SECONDS)
                .build()

        mockWebServer.enqueue(slowResponse)

        // When
        val result = bookClient.getBookByIsbn(isbn)

        // Then
        StepVerifier.create(result).verifyComplete()

        assertEquals(expectedNoRetryRequestCount, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog keeps returning server errors, then returns no book after exhausting retries`() {
        // Given
        val expectedExhaustingRetryRequestCount = 4

        val response = MockResponse(HttpStatus.SERVICE_UNAVAILABLE.value())
        repeat(4) { mockWebServer.enqueue(response) }

        // When
        val result = bookClient.getBookByIsbn("1234567890")

        // Then
        StepVerifier.create(result).verifyComplete()
        assertEquals(expectedExhaustingRetryRequestCount, mockWebServer.requestCount)
    }

    @Test
    fun `when book doesn't exist, then do not retry`() {
        // Given
        mockWebServer.enqueue(MockResponse(HttpStatus.NOT_FOUND.value()))

        // When
        val result = bookClient.getBookByIsbn("not-found-isbn")

        // Then
        StepVerifier.create(result).verifyComplete()
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog rejects request with client error, then returns no book without retrying`() {
        // Given
        val expectedNoRetryRequestCount = 1

        val response = MockResponse(HttpStatus.BAD_REQUEST.value())
        mockWebServer.enqueue(response)

        // When
        val result = bookClient.getBookByIsbn("invalid-isbn")

        // Then
        StepVerifier.create(result).verifyComplete()
        assertEquals(expectedNoRetryRequestCount, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog first fails and then reports not found, then returns no book without retrying further`() {
        // Given
        val serverUnavailableResponse = MockResponse(HttpStatus.SERVICE_UNAVAILABLE.value())
        val notFoundResponse = MockResponse(HttpStatus.NOT_FOUND.value())

        mockWebServer.enqueue(serverUnavailableResponse)
        mockWebServer.enqueue(notFoundResponse)

        // When
        val result = bookClient.getBookByIsbn("not-found-isbn")

        // Then
        StepVerifier.create(result).verifyComplete()
        assertEquals(2, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog returns malformed book data, then returns no book without retrying`() {
        // Given
        val malformedResponse = MockResponse(
            code = HttpStatus.OK.value(),
            headers = headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE),
            body = """{"isbn": ""invalid-json""}""",
        )

        mockWebServer.enqueue(malformedResponse)

        // When
        val result = bookClient.getBookByIsbn("1234567890")

        // Then
        StepVerifier.create(result).verifyComplete()
        assertEquals(1, mockWebServer.requestCount)
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
