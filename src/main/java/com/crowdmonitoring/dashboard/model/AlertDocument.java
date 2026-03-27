package com.crowdmonitoring.dashboard.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "alerts")
public class AlertDocument {
  @Id
  private String id;

  private String zone;
  private String message;
  private String severity; // High | Medium | Low

  private Instant timestamp;

  private boolean isActive;

  private double intensitySnapshot; // convenience
  private double rssiSnapshot; // convenience
}

