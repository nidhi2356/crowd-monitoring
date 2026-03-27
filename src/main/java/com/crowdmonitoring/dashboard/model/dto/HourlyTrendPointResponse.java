package com.crowdmonitoring.dashboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyTrendPointResponse {
  private String label; // e.g. 14:00
  private long value; // total crowd for hour bucket
}

