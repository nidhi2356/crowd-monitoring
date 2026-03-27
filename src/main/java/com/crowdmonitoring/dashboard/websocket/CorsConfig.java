package com.crowdmonitoring.dashboard.websocket;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${crowd.cors.allowedOrigins:*}")
  private String allowedOrigins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins = allowedOrigins == null || allowedOrigins.isBlank()
        ? new String[] { "*" }
        : allowedOrigins.split(",");

    var mapping = registry.addMapping("/**")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");

    if (origins.length == 1 && origins[0].trim().equals("*")) {
      mapping.allowedOriginPatterns("*");
    } else {
      mapping.allowedOrigins(Arrays.stream(origins).map(String::trim).toArray(String[]::new));
    }

    // Credentials are not required for this dashboard.
    mapping.allowCredentials(false);
  }
}

