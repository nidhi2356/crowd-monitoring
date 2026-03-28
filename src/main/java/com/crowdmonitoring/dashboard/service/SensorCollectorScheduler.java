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
import com.crowdmonitoring.dashboard.repository.*;

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

      // STEP 1: Get buffered AI data
      Map<String, List<AiSensorPayload>> buffered =
              zoneDataBuffer.drainAndClear();

      if (buffered.isEmpty()) {
        log.warn("No AI data received in last cycle");
        return;
      }

      long totalCrowd = 0;
      double totalEntryRate = 0;
      double totalExitRate = 0;
      long activeSensors = 0;

      Map<String, double[]> coords =
              zoneRegistry.getZoneCoordinates();

      // STEP 2: Process each zone
      for (Map.Entry<String, List<AiSensorPayload>> entry : buffered.entrySet()) {

        String zone = entry.getKey();
        List<AiSensorPayload> payloads = entry.getValue();

        if (payloads.isEmpty()) continue;

        // ===== CALCULATIONS =====

        // Average Crowd
        double avgCrowd = payloads.stream()
                .mapToLong(AiSensorPayload::getTotalCrowd)
                .average().orElse(0);

        // Peak Crowd
        long peakCrowd = payloads.stream()
                .mapToLong(AiSensorPayload::getTotalCrowd)
                .max().orElse(0);

        // Intensity
        double avgIntensity = payloads.stream()
                .mapToDouble(p -> p.getTotalCrowd() / 250.0)
                .average().orElse(0);

        // Network Score
        double avgNetworkScore = payloads.stream()
                .mapToDouble(p -> ((p.getAvgRSSI() + 90.0) / 30.0) * 100.0)
                .average().orElse(0);

        // Risk Score
        double avgRiskScore =
                (0.65 * avgIntensity * 100.0) +
                        (0.35 * (100.0 - avgNetworkScore));

        // Entry / Exit Rate
        double avgEntryRate = 0;
        double avgExitRate = 0;

        for (int i = 1; i < payloads.size(); i++) {
          long prev = payloads.get(i - 1).getTotalCrowd();
          long curr = payloads.get(i).getTotalCrowd();

          if (curr > prev) avgEntryRate += (curr - prev);
          else avgExitRate += (prev - curr);
        }

        avgEntryRate /= payloads.size();
        avgExitRate /= payloads.size();

        // Avg RSSI
        double avgRSSI = payloads.stream()
                .mapToDouble(AiSensorPayload::getAvgRSSI)
                .average().orElse(0);

        // Density
        AiSensorPayload last = payloads.get(payloads.size() - 1);
        long crowd = last.getTotalCrowd();

        String density;
        if (crowd < 50) density = "Low";
        else if (crowd <= 150) density = "Medium";
        else density = "High";

        // ===== SAVE CROWD DATA =====
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

        // ===== SAVE HEATMAP =====
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
        activeSensors += 2;
      }

      // ===== ANALYTICS =====
      analyticsEngineService.updateAggregates(
              totalCrowd, minuteTs, hourlyRepo, dailyRepo
      );

      // ===== SENSOR STATUS =====
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

      log.info("Aggregation complete. Zones: {}, Total Crowd: {}",
              buffered.size(), totalCrowd);

    } catch (Exception e) {
      log.error("Scheduler failed", e);
    }
  }
}