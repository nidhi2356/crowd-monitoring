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
public class AlertsResponse {
  private List<AlertResponse> activeAlerts;
  private List<AlertResponse> recentAlerts;
  private List<AlertResponse> alertHistory;
}

