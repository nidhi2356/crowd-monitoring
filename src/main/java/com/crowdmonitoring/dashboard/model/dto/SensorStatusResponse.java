package com.crowdmonitoring.dashboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorStatusResponse {
  private String status;
  private long activeSensors;
  private long uptimeSeconds;
  private double entryRate;
  private double exitRate;
}

