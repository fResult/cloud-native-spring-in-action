package com.polarbookshop.catalogservice.common.config;

import io.vavr.jackson.datatype.VavrModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;

@Configuration(proxyBeanMethods = false)
public class JacksonConfiguration {

  @Bean
  public JacksonModule vavrModule() {
    return new VavrModule();
  }
}
