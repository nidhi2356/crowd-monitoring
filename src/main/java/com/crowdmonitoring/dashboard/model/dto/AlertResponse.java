package com.crowdmonitoring.dashboard.model.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
  private String zone;
  private String message;
  private String severity; // High | Medium | Low
  private Instant timestamp;
  private boolean isActive;

  private double intensitySnapshot;
  private double rssiSnapshot;
}

