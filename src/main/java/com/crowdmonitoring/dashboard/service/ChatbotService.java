package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.dto.AlertsResponse;
import com.crowdmonitoring.dashboard.model.dto.AnalyticsResponse;
import com.crowdmonitoring.dashboard.model.dto.ChatbotResponse;
import com.crowdmonitoring.dashboard.model.dto.DashboardResponse;
import com.crowdmonitoring.dashboard.model.dto.HeatmapResponse;
import com.crowdmonitoring.dashboard.model.dto.ZoneStatsResponse;
import com.crowdmonitoring.dashboard.repository.AlertRepository;
import com.crowdmonitoring.dashboard.repository.CrowdMinuteDataRepository;
import com.crowdmonitoring.dashboard.repository.DailySummaryRepository;
import com.crowdmonitoring.dashboard.repository.HeatmapDataRepository;
import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;
import com.crowdmonitoring.dashboard.repository.SensorStatusRepository;

@Service
public class ChatbotService {

  private final ZoneRegistry zoneRegistry = new ZoneRegistry();

  private final DashboardQueryService dashboardQueryService;
  private final AlertsEngineService alertsEngineService;
  private final AnalyticsEngineService analyticsEngineService;
  private final HeatmapDataService heatmapDataService;

  private final CrowdMinuteDataRepository crowdRepo;
  private final AlertRepository alertRepo;
  private final HourlyDataRepository hourlyRepo;
  private final DailySummaryRepository dailyRepo;
  private final HeatmapDataRepository heatmapRepo;
  private final SensorStatusRepository sensorStatusRepo;

  public ChatbotService(
      DashboardQueryService dashboardQueryService,
      AlertsEngineService alertsEngineService,
      AnalyticsEngineService analyticsEngineService,
      HeatmapDataService heatmapDataService,
      CrowdMinuteDataRepository crowdRepo,
      AlertRepository alertRepo,
      HourlyDataRepository hourlyRepo,
      DailySummaryRepository dailyRepo,
      HeatmapDataRepository heatmapRepo,
      SensorStatusRepository sensorStatusRepo
  ) {
    this.dashboardQueryService = dashboardQueryService;
    this.alertsEngineService = alertsEngineService;
    this.analyticsEngineService = analyticsEngineService;
    this.heatmapDataService = heatmapDataService;
    this.crowdRepo = crowdRepo;
    this.alertRepo = alertRepo;
    this.hourlyRepo = hourlyRepo;
    this.dailyRepo = dailyRepo;
    this.heatmapRepo = heatmapRepo;
    this.sensorStatusRepo = sensorStatusRepo;
  }

  public ChatbotResponse getReply(String message) {
    String q = message == null ? "" : message.toLowerCase(Locale.ROOT);

    Instant now = Instant.now();
    DashboardResponse dashboard = dashboardQueryService.buildDashboardResponse(
        crowdRepo,
        alertRepo,
        sensorStatusRepo,
        hourlyRepo,
        now
    );
    AlertsResponse alerts = alertsEngineService.buildAlertsResponse(alertRepo);
    AnalyticsResponse analytics = analyticsEngineService.buildAnalyticsResponse(hourlyRepo, dailyRepo, now);
    HeatmapResponse heatmap = heatmapDataService.getRealtimeHeatmap();

    List<ZoneStatsResponse> zones = dashboard.getZones() == null ? List.of() : dashboard.getZones();

    // Active alert highlights.
    List<String> overcrowdedZones = alerts.getActiveAlerts() == null
        ? List.of()
        : alerts.getActiveAlerts().stream()
            .filter(a -> "High".equalsIgnoreCase(a.getSeverity()))
            .map(a -> a.getZone())
            .distinct()
            .toList();

    boolean weakNetworkActive = alerts.getActiveAlerts() != null &&
        alerts.getActiveAlerts().stream()
            .anyMatch(a -> "Medium".equalsIgnoreCase(a.getSeverity()) && !a.getZone().isBlank());

    if (q.contains("overcrowd") || q.contains("crowd") && q.contains("high")) {
      if (!overcrowdedZones.isEmpty()) {
        return ChatbotResponse.builder()
            .reply("Overcrowding is currently active in: " + String.join(", ", overcrowdedZones) +
                ". Total crowd is " + dashboard.getTotalCrowd() + ". Peak in the last 24h: " + analytics.getPeakCrowd() + ".")
            .build();
      }
      return ChatbotResponse.builder()
          .reply("No active overcrowding alerts right now. Total crowd is " + dashboard.getTotalCrowd() +
              ". Peak in the last 24h: " + analytics.getPeakCrowd() + ".")
          .build();
    }

    if (q.contains("weak") || q.contains("network") || q.contains("rssi")) {
      if (weakNetworkActive) {
        return ChatbotResponse.builder()
            .reply("Weak network conditions are active (RSSI below threshold). Network score right now is " +
                String.format(Locale.ROOT, "%.1f", dashboard.getNetworkScore()) + "/100. Consider prioritizing connectivity for critical zones.")
            .build();
      }
      return ChatbotResponse.builder()
          .reply("Network looks healthy at the moment. Network score is " +
              String.format(Locale.ROOT, "%.1f", dashboard.getNetworkScore()) + "/100.")
          .build();
    }

    if (q.contains("risk") || q.contains("zone") || q.contains("density")) {
      String topRisk = zones.stream()
          .sorted(Comparator.comparingDouble(ZoneStatsResponse::getRiskScore).reversed())
          .limit(3)
          .map(z -> z.getZoneName() + " (risk " + Math.round(z.getRiskScore()) + ")")
          .collect(Collectors.joining(", "));
      return ChatbotResponse.builder()
          .reply("Top risk zones: " + topRisk + ". Current heatmap intensity points: " +
              (heatmap.getPoints() == null ? 0 : heatmap.getPoints().size()) + ".")
          .build();
    }

    if (q.contains("busiest") || q.contains("peak day") || q.contains("day")) {
      return ChatbotResponse.builder()
          .reply("Busiest day (by average traffic): " + analytics.getBusiestDay() + ". Peak crowd (last 24h): " + analytics.getPeakCrowd() + ".")
          .build();
    }

    return ChatbotResponse.builder()
        .reply("I can summarize current crowd intensity, active alerts, and network health. Ask: 'overcrowding', 'weak network', or 'zone risk'.")
        .build();
  }
}

