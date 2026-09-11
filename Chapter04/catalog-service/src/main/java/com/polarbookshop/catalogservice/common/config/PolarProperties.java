package com.polarbookshop.catalogservice.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** @param greeting A message to welcome users. */
@ConfigurationProperties(prefix = "polar")
public record PolarProperties(String greeting) {}
