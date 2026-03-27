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
@Document(collection = "crowd_minute_data")
public class CrowdMinuteData {

  @Id
  private String id;

  private String zone;
  private Instant timestamp;

  // Business logic outputs (per zone).
  private long totalCrowd;
  private String density; // Low | Medium | High
  private double intensity; // 0..1
  private double networkScore; // 0..100
  private double riskScore; // 0..100

  private double entryRate; // people/min
  private double exitRate; // people/min

  // Raw-ish inputs retained for debugging/analytics.
  private double avgRssi;
  private int wifiCount;
  private int cameraCount;
}

