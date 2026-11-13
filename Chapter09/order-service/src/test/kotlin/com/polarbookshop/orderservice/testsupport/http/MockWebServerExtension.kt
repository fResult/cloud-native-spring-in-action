package com.polarbookshop.orderservice.testsupport.http

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

internal fun MockWebServer.responseWith(mapper: ObjectMapper) = JsonResponseEnqueuer(this, mapper)

internal fun MockWebServer.delayedResponseWith(mapper: ObjectMapper) = JsonDelayedResponseEnqueuer(this, mapper)

internal class JsonResponseEnqueuer(
    private val mockWebServer: MockWebServer,
    private val mapper: ObjectMapper,
) {
    operator fun <T> invoke(
        response: T? = null,
        status: HttpStatus = HttpStatus.OK,
    ) {
        val mockResponse =
            MockResponse(
                code = status.value(),
                headers = headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE),
                body = mapper.writeValueAsString(response),
            )

        mockWebServer.enqueue(mockResponse)
    }
}

internal class JsonDelayedResponseEnqueuer(
    private val mockWebServer: MockWebServer,
    private val mapper: ObjectMapper,
) {
    operator fun <T> invoke(
        response: T? = null,
        delay: Long,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
    ) {
        val mockResponse =
            MockResponse
                .Builder()
                .code(HttpStatus.OK.value())
                .headers(headersOf(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .body(mapper.writeValueAsString(response))
                .headersDelay(delay, timeUnit)
                .build()

        mockWebServer.enqueue(mockResponse)
    }
}
