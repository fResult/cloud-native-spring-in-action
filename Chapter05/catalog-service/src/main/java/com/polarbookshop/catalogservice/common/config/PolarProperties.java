package com.polarbookshop.catalogservice.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "polar")
public class PolarProperties {
  /** A message to welcome users */
  public String greeting;
}
