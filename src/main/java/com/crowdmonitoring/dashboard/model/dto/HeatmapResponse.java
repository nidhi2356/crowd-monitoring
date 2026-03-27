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
public class HeatmapResponse {
  private List<HeatmapPointResponse> points;
}

