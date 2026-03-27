package com.crowdmonitoring.dashboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneStatsResponse {
  private String zoneName;
  private long crowdCount;
  private String density; // Low | Medium | High
  private double riskScore;

  private double entryRate;
  private double exitRate;
}

