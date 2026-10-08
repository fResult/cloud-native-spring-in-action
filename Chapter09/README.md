# Chapter 9 – API Gateway and Circuit Breakers

This chapter expands the Polar Bookshop system with an **Edge Service**: an API gateway that provides a single external entry point while keeping the internal Catalog and Order service APIs decoupled from clients.

Using Spring Cloud Gateway and the reactive Spring stack, you will define routes, route predicates, and filters for forwarding requests to the application services.\
The gateway is also a natural place to handle cross-cutting concerns, including resilience, traffic control, and session management.

You will make service calls more fault-tolerant with Spring Cloud Circuit Breaker and Resilience4J, combining circuit breakers with retries, time limiters, and fallback REST APIs.\
The chapter then uses Redis for request rate limiting and distributed web sessions, before introducing Kubernetes Ingress for managing external traffic into the cluster.

Original source code: [Thomas Vitale's Chapter 9 final project](https://github.com/ThomasVitale/cloud-native-spring-in-action/tree/main/Chapter09/09-end).
