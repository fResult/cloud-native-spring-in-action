package com.polarbookshop.orderservice.book.web

import com.polarbookshop.orderservice.book.Book
import com.polarbookshop.orderservice.config.BookHttpApi
import com.polarbookshop.orderservice.config.CatalogServiceProperties
import com.polarbookshop.orderservice.testsupport.http.delayedResponseWith
import com.polarbookshop.orderservice.testsupport.http.responseWith
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class CatalogBookClientTest {
    @field:StartStop
    private val mockWebServer: MockWebServer = MockWebServer()

    val mapper = ObjectMapper()
    private val responseWith = mockWebServer.responseWith(mapper)
    private val delayedResponseWith = mockWebServer.delayedResponseWith(mapper)

    private val bookHttpApi by lazy {
        val webClient = WebClient.create(mockWebServer.url("/").toString())
        val adapter = WebClientAdapter.create(webClient)

        return@lazy HttpServiceProxyFactory.builderFor(adapter).build().createClient(BookHttpApi::class.java)
    }

    private val bookClient by lazy(::createBookClient)

    private fun createBookClient(responseTimeout: Duration = 5.seconds): CatalogBookClient {
        val properties =
            CatalogServiceProperties().apply {
                this.uri = URI.create("http://localhost:${mockWebServer.port}")
                this.responseTimeout = responseTimeout.toJavaDuration()
            }

        return CatalogBookClient(bookHttpApi, properties)
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
        responseWith(expectedBook, status = HttpStatus.OK)

        // When
        val actualBook = bookClient.getBookByIsbn(expectedBook.isbn)

        // Then
        StepVerifier.create(actualBook).expectNext(expectedBook).verifyComplete()

        val request = mockWebServer.takeRequest()
        assertEquals(HttpMethod.GET.name(), request.method)
        assertEquals("/books/${expectedBook.isbn}", request.target)
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
                .headers(headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
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
        val slowBookClient = createBookClient(responseTimeout = 500.milliseconds)
        delayedResponseWith<Book>(delay = 2, timeUnit = TimeUnit.SECONDS)

        // When
        val result = slowBookClient.getBookByIsbn(isbn)

        // Then
        StepVerifier.create(result).verifyComplete()

        assertEquals(expectedNoRetryRequestCount, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog response exceeds attempt timeout, then returns no book without retrying`() {
        // Given
        val expectedNoRetryRequestCount = 1

        val maxAllowedWaitDuration = 10.milliseconds
        val slowResponseDelayMs = 100L
        val isbn = "1234567890"
        val expectedBook = Book(isbn, "Title", "Author", BigDecimal("9.90"))
        val timeoutBookClient = createBookClient(responseTimeout = maxAllowedWaitDuration)

        delayedResponseWith(expectedBook, delay = slowResponseDelayMs, timeUnit = TimeUnit.MILLISECONDS)
        responseWith(expectedBook)

        // When
        val result = timeoutBookClient.getBookByIsbn(isbn)

        // Then
        StepVerifier.create(result).verifyComplete()
        assertEquals(expectedNoRetryRequestCount, mockWebServer.requestCount)
    }

    @Test
    fun `when catalog responds before attempt timeout, then returns the book without retrying`() {
        // Given
        val expectedRequestCount = 1

        val isbn = "1234567890"
        val expectedBook = Book(isbn, "Title", "Author", BigDecimal("9.90"))
        val timeoutBookClient = createBookClient(responseTimeout = 3.seconds)
        delayedResponseWith(expectedBook, delay = 1)

        // When
        val result = timeoutBookClient.getBookByIsbn(isbn)

        // Then
        StepVerifier.create(result).expectNext(expectedBook).verifyComplete()
        assertEquals(expectedRequestCount, mockWebServer.requestCount)
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
    fun `when catalog returns 404, then returns no book without retrying`() {
        // Given
        val response = MockResponse(HttpStatus.NOT_FOUND.value())
        mockWebServer.enqueue(response)

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
        val malformedResponse =
            MockResponse(
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
