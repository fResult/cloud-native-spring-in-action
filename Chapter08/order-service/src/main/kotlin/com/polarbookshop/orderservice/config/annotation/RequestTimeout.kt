package com.polarbookshop.orderservice.config.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestTimeout(
    val seconds: Int,
)
