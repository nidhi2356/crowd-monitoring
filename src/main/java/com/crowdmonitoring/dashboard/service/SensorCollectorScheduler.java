package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.CrowdMinuteData;
import com.crowdmonitoring.dashboard.model.HeatmapDataPoint;
import com.crowdmonitoring.dashboard.model.SensorStatusDocument;
import com.crowdmonitoring.dashboard.model.dto.AiSensorPayload;
import com.crowdmonitoring.dashboard.repository.AlertRepository;
import com.crowdmonitoring.dashboard.repository.CrowdMinuteDataRepository;
import com.crowdmonitoring.dashboard.repository.DailySummaryRepository;
import com.crowdmonitoring.dashboard.repository.HeatmapDataRepository;
import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;
import com.crowdmonitoring.dashboard.repository.SensorStatusRepository;

/**
 * Runs every 60 seconds.
 *
 * Responsibility:
 * 1. Drain the ZoneDataBuffer (filled by AI WebSocket messages)
 * 2. Compute 60-second aggregates per zone
 * 3. Write aggregated results to MongoDB
 * 4. Clear buffer
 *
 * Does NOT fetch sensor data (AI does that).
 * Does NOT compute crowd metrics (AI does that).
 * Does NOT broadcast to frontend (AiDataIngestionController does that live).
 */
@Service
public class SensorCollectorScheduler {

  private static final Logger log =
          LoggerFactory.getLogger(SensorCollectorScheduler.class);

  private final ZoneDataBuffer zoneDataBuffer;
  private final ZoneRegistry zoneRegistry;
  private final AnalyticsEngineService analyticsEngineService;
  private final CrowdMinuteDataRepository crowdRepo;
  private final HeatmapDataRepository heatmapRepo;
  private final HourlyDataRepository hourlyRepo;
  private final DailySummaryRepository dailyRepo;
  private final SensorStatusRepository sensorStatusRepo;
  private final AlertRepository alertRepo;

  private final long appStartMillis = System.currentTimeMillis();

  public SensorCollectorScheduler(
          ZoneDataBuffer zoneDataBuffer,
          ZoneRegistry zoneRegistry,
          AnalyticsEngineService analyticsEngineService,
          CrowdMinuteDataRepository crowdRepo,
          HeatmapDataRepository heatmapRepo,
          HourlyDataRepository hourlyRepo,
          DailySummaryRepository dailyRepo,
          SensorStatusRepository sensorStatusRepo,
          AlertRepository alertRepo
  ) {
    this.zoneDataBuffer = zoneDataBuffer;
    this.zoneRegistry = zoneRegistry;
    this.analyticsEngineService = analyticsEngineService;
    this.crowdRepo = crowdRepo;
    this.heatmapRepo = heatmapRepo;
    this.hourlyRepo = hourlyRepo;
    this.dailyRepo = dailyRepo;
    this.sensorStatusRepo = sensorStatusRepo;
    this.alertRepo = alertRepo;
  }

  @Scheduled(fixedDelayString = "${crowd.scheduler.fixedDelayMs:60000}")
  public void aggregateAndStore() {
    try {
      Instant now = Instant.now();
      Instant minuteTs = now.truncatedTo(ChronoUnit.MINUTES);

      // Step 1: Drain buffer — get all AI payloads from last 60s
      Map<String, List<AiSensorPayload>> buffered =
              zoneDataBuffer.drainAndClear();

      if (buffered.isEmpty()) {
        log.warn("Buffer empty at aggregation time — no AI data received");
        return;
      }

      long totalCrowd = 0;
      double totalEntryRate = 0;
      double totalExitRate = 0;
      long activeSensors = 0;
      Map<String, double[]> coords = zoneRegistry.getZoneCoordinates();

      // Step 2: Aggregate per zone
      for (Map.Entry<String, List<AiSensorPayload>> entry
              : buffered.entrySet()) {

        String zone = entry.getKey();
        List<AiSensorPayload> payloads = entry.getValue();

        if (payloads.isEmpty()) continue;

        // Compute averages over the 60s window
        double avgCrowd = payloads.stream()
                .mapToLong(AiSensorPayload::getTotalCrowd)
                .average().orElse(0);

        long peakCrowd = payloads.stream()
                .mapToLong(AiSensorPayload::getTotalCrowd)
                .max().orElse(0);

        double avgIntensity = payloads.stream()
                .mapToDouble(AiSensorPayload::getIntensity)
                .average().orElse(0);

        double avgRiskScore = payloads.stream()
                .mapToDouble(AiSensorPayload::getRiskScore)
                .average().orElse(0);

        double avgNetworkScore = payloads.stream()
                .mapToDouble(AiSensorPayload::getNetworkScore)
                .average().orElse(0);

        double avgRSSI = payloads.stream()
                .mapToDouble(AiSensorPayload::getAvgRSSI)
                .average().orElse(0);

        double avgEntryRate = payloads.stream()
                .mapToDouble(AiSensorPayload::getEntryRate)
                .average().orElse(0);

        double avgExitRate = payloads.stream()
                .mapToDouble(AiSensorPayload::getExitRate)
                .average().orElse(0);

        // Use last payload's density (most recent)
        AiSensorPayload last = payloads.get(payloads.size() - 1);
        String density = last.getDensity();

        // Step 3: Write crowd_minute_data to MongoDB
        CrowdMinuteData doc = CrowdMinuteData.builder()
                .zone(zone)
                .timestamp(minuteTs)
                .totalCrowd(Math.round(avgCrowd))
                .density(density)
                .intensity(avgIntensity)
                .networkScore(avgNetworkScore)
                .riskScore(avgRiskScore)
                .entryRate(avgEntryRate)
                .exitRate(avgExitRate)
                .avgRssi(avgRSSI)
                .wifiCount(last.getWifiCount())
                .cameraCount(last.getCameraCount())
                .build();

        crowdRepo.save(doc);

        // Step 4: Write heatmap_data to MongoDB
        double[] latLng = coords.get(zone);
        if (latLng != null) {
          HeatmapDataPoint heatPoint = HeatmapDataPoint.builder()
                  .zone(zone)
                  .timestamp(minuteTs)
                  .lat(latLng[0])
                  .lng(latLng[1])
                  .intensity(avgIntensity)
                  .build();
          heatmapRepo.save(heatPoint);
        }

        totalCrowd += Math.round(avgCrowd);
        totalEntryRate += avgEntryRate;
        totalExitRate += avgExitRate;
        activeSensors += 2; // camera + wifi per zone
      }

      // Step 5: Update hourly_data and daily_summary
      analyticsEngineService.updateAggregates(
              totalCrowd, minuteTs, hourlyRepo, dailyRepo
      );

      // Step 6: Write sensor_status to MongoDB
      long uptimeSeconds =
              (System.currentTimeMillis() - appStartMillis) / 1000;

      SensorStatusDocument statusDoc = SensorStatusDocument.builder()
              .timestamp(minuteTs)
              .status(activeSensors > 0 ? "Operational" : "Offline")
              .activeSensors(activeSensors)
              .uptimeSeconds(uptimeSeconds)
              .entryRate(totalEntryRate)
              .exitRate(totalExitRate)
              .build();

      sensorStatusRepo.save(statusDoc);

      log.info("Aggregation complete — zones: {}, totalCrowd: {}",
              buffered.size(), totalCrowd);

    } catch (Exception e) {
      log.error("Aggregation scheduler failed", e);
    }
  }
}