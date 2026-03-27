package com.crowdmonitoring.dashboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatmapPointResponse {
  private double lat;
  private double lng;
  private double intensity; // 0..1
}

