package com.crowdmonitoring.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrowdMonitoringApplication {
  public static void main(String[] args) {
    SpringApplication.run(CrowdMonitoringApplication.class, args);
  }
}

