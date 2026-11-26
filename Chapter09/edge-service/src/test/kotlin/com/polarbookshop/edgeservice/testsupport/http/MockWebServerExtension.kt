package com.polarbookshop.edgeservice.testsupport.http

import mockwebserver3.MockWebServer

@Suppress("HttpUrlsUsage")
fun MockWebServer.httpUri() = "http://${this.hostName}:${this.port}"
