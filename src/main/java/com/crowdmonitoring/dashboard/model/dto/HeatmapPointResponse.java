package com.crowdmonitoring.dashboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatmapPointResponse {
  private String zone;
  private double lat;
  private double lng;
  private double intensity; // 0..1
  private Instant timestamp;
}

