package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.AlertDocument;
import com.crowdmonitoring.dashboard.model.dto.AlertResponse;
import com.crowdmonitoring.dashboard.model.dto.AlertsResponse;
import com.crowdmonitoring.dashboard.repository.AlertRepository;

@Service
public class AlertsEngineService {

  private static final String OVERCROWDING_MSG = "Overcrowding detected";
  private static final String WEAK_NETWORK_MSG = "Weak Network (RSSI below threshold)";

  /**
   * Called every 60s by SensorCollectorScheduler
   * for batch alert evaluation from aggregated data.
   */
  public void evaluateAndPersist(
          String zone,
          double intensity,
          double avgRSSI,
          Instant now,
          AlertRepository alertRepository
  ) {
    // Overcrowding check
    persistCondition(
            alertRepository, zone,
            OVERCROWDING_MSG, "High",
            intensity > 0.85,
            intensity, avgRSSI, now
    );

    // Weak network check
    persistCondition(
            alertRepository, zone,
            WEAK_NETWORK_MSG, "Medium",
            avgRSSI < -80.0,
            intensity, avgRSSI, now
    );
  }

  /**
   * Called in real-time by AiDataIngestionController
   * on every AI WebSocket message — for live alert checking.
   */
  public void evaluateRealTime(
          String zone,
          double intensity,
          double avgRSSI,
          Instant now,
          AlertRepository alertRepository
  ) {
    // Same logic, called live per AI message
    evaluateAndPersist(zone, intensity, avgRSSI, now, alertRepository);
  }

  /**
   * FIX: Only create a new alert if no active one exists.
   * Old code was creating duplicate alerts every minute.
   */
  private void persistCondition(
          AlertRepository alertRepository,
          String zone,
          String message,
          String severity,
          boolean conditionActive,
          double intensitySnapshot,
          double rssiSnapshot,
          Instant now
  ) {
    List<AlertDocument> existingActive =
            alertRepository.findByZoneAndMessageAndSeverityAndIsActiveTrue(
                    zone, message, severity
            );

    if (!conditionActive) {
      // Condition resolved — mark existing alerts inactive
      if (!existingActive.isEmpty()) {
        existingActive.forEach(d -> d.setActive(false));
        alertRepository.saveAll(existingActive);
      }
      return;
    }

    // Condition active — only create if none already active
    // FIX: was creating new alert every minute even if same condition
    if (existingActive.isEmpty()) {
      AlertDocument doc = AlertDocument.builder()
              .zone(zone)
              .message(message)
              .severity(severity)
              .timestamp(now)
              .isActive(true)
              .intensitySnapshot(intensitySnapshot)
              .rssiSnapshot(rssiSnapshot)
              .build();
      alertRepository.save(doc);
    }
  }

  public AlertsResponse buildAlertsResponse(AlertRepository alertRepository) {
    List<AlertDocument> active =
            alertRepository.findByIsActiveTrueOrderByTimestampDesc();
    List<AlertDocument> history =
            alertRepository.findTop20ByOrderByTimestampDesc();

    List<AlertResponse> activeMapped = active.stream()
            .map(this::toResponse).toList();

    List<AlertResponse> historyMapped = history.stream()
            .map(this::toResponse).toList();

    List<AlertResponse> recent = historyMapped.stream()
            .limit(10).toList();

    return AlertsResponse.builder()
            .activeAlerts(activeMapped)
            .recentAlerts(recent)
            .alertHistory(historyMapped)
            .build();
  }

  private AlertResponse toResponse(AlertDocument doc) {
    return AlertResponse.builder()
            .zone(doc.getZone())
            .message(doc.getMessage())
            .severity(doc.getSeverity())
            .timestamp(doc.getTimestamp())
            .isActive(doc.isActive())
            .intensitySnapshot(doc.getIntensitySnapshot())
            .rssiSnapshot(doc.getRssiSnapshot())
            .build();
  }
}

