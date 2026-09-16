package com.polarbookshop.catalogservice;

import com.polarbookshop.catalogservice.common.config.PolarProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PolarProperties.class)
public class CatalogServiceApplication {
  static void main(String[] args) {
    SpringApplication.run(CatalogServiceApplication.class, args);
  }
}
