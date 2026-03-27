package com.crowdmonitoring.dashboard.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
  private long totalCrowd;
  private double networkScore; // 0..100
  private long peakCrowd;
  private List<ZoneStatsResponse> zones;
  private SensorStatusResponse sensorStatus;
}

