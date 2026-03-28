package com.crowdmonitoring.dashboard.controller;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crowdmonitoring.dashboard.model.dto.AlertsResponse;
import com.crowdmonitoring.dashboard.model.dto.AnalyticsResponse;
import com.crowdmonitoring.dashboard.model.dto.DashboardResponse;
import com.crowdmonitoring.dashboard.model.dto.HeatmapResponse;
import com.crowdmonitoring.dashboard.repository.AlertRepository;
import com.crowdmonitoring.dashboard.repository.CrowdMinuteDataRepository;
import com.crowdmonitoring.dashboard.repository.DailySummaryRepository;
import com.crowdmonitoring.dashboard.repository.HeatmapDataRepository;
import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;
import com.crowdmonitoring.dashboard.repository.SensorStatusRepository;
import com.crowdmonitoring.dashboard.service.AlertsEngineService;
import com.crowdmonitoring.dashboard.service.AnalyticsEngineService;
import com.crowdmonitoring.dashboard.service.DashboardQueryService;
import com.crowdmonitoring.dashboard.service.HeatmapDataService;

@RestController
public class DashboardController {

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

  public DashboardController(
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

  // ─── GET ENDPOINTS (initial page load) ───────────────────────

  @GetMapping("/dashboard")
  public DashboardResponse getDashboard() {
    return dashboardQueryService.buildDashboardResponse(
            crowdRepo, alertRepo, sensorStatusRepo,
            hourlyRepo, Instant.now()
    );
  }

  @GetMapping("/alerts")
  public AlertsResponse getAlerts() {
    return alertsEngineService.buildAlertsResponse(alertRepo);
  }

  @GetMapping("/analytics")
  public AnalyticsResponse getAnalytics() {
    return analyticsEngineService.buildAnalyticsResponse(
            hourlyRepo, dailyRepo, Instant.now()
    );
  }

  @GetMapping("/heatmap")
  public HeatmapResponse getHeatmap() {
    return heatmapDataService.getRealtimeHeatmap();
  }

  // ─── PUT ENDPOINTS (backend pushes updated data) ─────────────

  /**
   * Called when backend wants to push latest dashboard
   * summary to frontend (e.g. after aggregation completes).
   * Frontend can also poll this if WebSocket is unavailable.
   */
  @PutMapping("/dashboard")
  public DashboardResponse pushDashboard() {
    return dashboardQueryService.buildDashboardResponse(
            crowdRepo, alertRepo, sensorStatusRepo,
            hourlyRepo, Instant.now()
    );
  }

  /**
   * Called when backend wants to push latest alert state.
   * Useful for alert panel refresh after alert resolution.
   */
  @PutMapping("/alerts")
  public AlertsResponse pushAlerts() {
    return alertsEngineService.buildAlertsResponse(alertRepo);
  }
}