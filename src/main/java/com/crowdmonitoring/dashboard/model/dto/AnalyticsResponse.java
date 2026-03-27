// package com.crowdmonitoring.dashboard.model.dto;

// import java.util.List;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class AnalyticsResponse {
//   private List<HourlyTrendPointResponse> hourlyTrend; // 24 points
//   private long peakCrowd;
//   private double averageTraffic; // average crowd over last 24h
//   private String busiestDay;
// }

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
public class AnalyticsResponse {
  private List<HourlyTrendPointResponse> hourlyTrend; // 24 points
  private long peakCrowd;
  private double averageTraffic; // average crowd over last 24h
  private String busiestDay;

  // New fields for updated frontend
  private long peakControl;
  private double avgDailyTraffic;
  private String busiestDayOfWeek;
  private List<HourlyTrendPointResponse> hourlyEntryExit;
  private List<HourlyTrendPointResponse> weeklyCrowdTrend;
}