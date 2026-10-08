# Chapter 10 - Event-Driven Applications and Functions

This chapter moves the Polar Bookshop system beyond synchronous request/response communication and introduces event-driven architecture.\
Instead of requiring services to remain available at the same time, services produce events and consume them asynchronously, which reduces temporal coupling.

You will use RabbitMQ as the message broker for publish/subscribe communication.\
Then, you will implement business logic as functions with Spring Cloud Function and expose those functions through REST APIs, serverless platforms, or data streams.

With Spring Cloud Stream, you will bind functions to RabbitMQ message channels, test message processing, and make messaging more resilient to failures.\
The chapter also covers event consumers and producers, including idempotency for safe event consumption and atomicity for reliable event production.

Original source code: [Thomas Vitale's Chapter 10 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter10/10-end).
