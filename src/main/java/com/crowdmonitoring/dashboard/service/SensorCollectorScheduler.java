package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.CrowdMinuteData;
import com.crowdmonitoring.dashboard.model.SensorStatusDocument;
import com.crowdmonitoring.dashboard.model.dto.AlertsResponse;
import com.crowdmonitoring.dashboard.model.dto.DashboardResponse;
import com.crowdmonitoring.dashboard.model.dto.HeatmapResponse;
import com.crowdmonitoring.dashboard.repository.AlertRepository;
import com.crowdmonitoring.dashboard.repository.CrowdMinuteDataRepository;
import com.crowdmonitoring.dashboard.repository.DailySummaryRepository;
import com.crowdmonitoring.dashboard.repository.HeatmapDataRepository;
import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;
import com.crowdmonitoring.dashboard.repository.SensorStatusRepository;
import com.crowdmonitoring.dashboard.websocket.RealTimeBroadcastService;

@Service
public class SensorCollectorScheduler {

  private static final Logger log = LoggerFactory.getLogger(SensorCollectorScheduler.class);

  private final ZoneRegistry zoneRegistry = new ZoneRegistry();

  private final SensorDataService sensorDataService;
  private final ZoneComputationService zoneComputationService;
  private final CrowdMinuteDataRepository crowdRepo;
  private final AlertRepository alertRepo;
  private final AlertsEngineService alertsEngineService;
  private final AnalyticsEngineService analyticsEngineService;
  private final HeatmapDataService heatmapDataService;
  private final HeatmapDataRepository heatmapRepo;
  private final HourlyDataRepository hourlyRepo;
  private final DailySummaryRepository dailyRepo;
  private final SensorStatusRepository sensorStatusRepo;
  private final DashboardQueryService dashboardQueryService;
  private final RealTimeBroadcastService broadcastService;

  private final long appStartMillis = System.currentTimeMillis();

  public SensorCollectorScheduler(
      SensorDataService sensorDataService,
      ZoneComputationService zoneComputationService,
      CrowdMinuteDataRepository crowdRepo,
      AlertRepository alertRepo,
      AlertsEngineService alertsEngineService,
      AnalyticsEngineService analyticsEngineService,
      HeatmapDataService heatmapDataService,
      HeatmapDataRepository heatmapRepo,
      HourlyDataRepository hourlyRepo,
      DailySummaryRepository dailyRepo,
      SensorStatusRepository sensorStatusRepo,
      DashboardQueryService dashboardQueryService,
      RealTimeBroadcastService broadcastService
  ) {
    this.sensorDataService = sensorDataService;
    this.zoneComputationService = zoneComputationService;
    this.crowdRepo = crowdRepo;
    this.alertRepo = alertRepo;
    this.alertsEngineService = alertsEngineService;
    this.analyticsEngineService = analyticsEngineService;
    this.heatmapDataService = heatmapDataService;
    this.heatmapRepo = heatmapRepo;
    this.hourlyRepo = hourlyRepo;
    this.dailyRepo = dailyRepo;
    this.sensorStatusRepo = sensorStatusRepo;
    this.dashboardQueryService = dashboardQueryService;
    this.broadcastService = broadcastService;
  }

  @Scheduled(fixedDelayString = "${crowd.scheduler.fixedDelayMs:60000}")
  public void collectAndBroadcast() {
    try {
      Instant now = Instant.now();
      Instant minuteTs = now.truncatedTo(ChronoUnit.MINUTES);

      List<String> zones = zoneRegistry.getZones();
      List<ZoneComputationResult> computed = new ArrayList<>();

      long totalCrowd = 0;
      double entryRate = 0.0;
      double exitRate = 0.0;
      long activeSensors = zones.size() * 2L; // camera + wifi

      for (String zone : zones) {
        var camera = sensorDataService.fetchCameraSnapshot(zone);
        var wifi = sensorDataService.fetchWifiProbeSnapshot(zone);

        CrowdMinuteData previous =
            crowdRepo.findTopByZoneOrderByTimestampDesc(zone).orElse(null);

        ZoneComputationResult z = zoneComputationService.compute(zone, camera, wifi, previous);
        computed.add(z);

        CrowdMinuteData doc = CrowdMinuteData.builder()
            .zone(z.zoneName())
            .timestamp(minuteTs)
            .totalCrowd(z.totalCrowd())
            .density(z.density())
            .intensity(z.intensity())
            .networkScore(z.networkScore())
            .riskScore(z.riskScore())
            .entryRate(z.entryRate())
            .exitRate(z.exitRate())
            .avgRssi(z.avgRssi())
            .wifiCount(z.wifiCount())
            .cameraCount(z.cameraCount())
            .build();

        crowdRepo.save(doc);

        totalCrowd += z.totalCrowd();
        entryRate += z.entryRate();
        exitRate += z.exitRate();
      }

      // Alerts + Heatmap + Analytics aggregation.
      alertsEngineService.evaluateAndPersist(computed, minuteTs, alertRepo);
      heatmapDataService.persistHeatmap(computed, minuteTs, heatmapRepo);
      analyticsEngineService.updateAggregates(totalCrowd, minuteTs, hourlyRepo, dailyRepo);

      // Persist sensor status snapshot.
      long uptimeSeconds = Math.max(0, (System.currentTimeMillis() - appStartMillis) / 1000);
      String status = activeSensors > 0 ? "Operational" : "Offline";
      SensorStatusDocument statusDoc = SensorStatusDocument.builder()
          .timestamp(minuteTs)
          .status(status)
          .activeSensors(activeSensors)
          .uptimeSeconds(uptimeSeconds)
          .entryRate(entryRate)
          .exitRate(exitRate)
          .build();
      sensorStatusRepo.save(statusDoc);

      // Build response payloads and push via WebSockets.
      DashboardResponse dashboardResponse = dashboardQueryService.buildDashboardResponse(
          crowdRepo,
          alertRepo,
          sensorStatusRepo,
          hourlyRepo,
          Instant.now()
      );
      AlertsResponse alertsResponse = alertsEngineService.buildAlertsResponse(alertRepo);
      HeatmapResponse heatmapResponse = heatmapDataService.getLatestHeatmap(heatmapRepo);

      broadcastService.broadcastDashboard(dashboardResponse);
      broadcastService.broadcastAlerts(alertsResponse);
      broadcastService.broadcastHeatmap(heatmapResponse);
    } catch (Exception e) {
      log.error("Sensor collector failed", e);
    }
  }
}

