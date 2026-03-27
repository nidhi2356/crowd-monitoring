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

  private static final String OVERCROWDING_MESSAGE = "Overcrowding detected";
  private static final String WEAK_NETWORK_MESSAGE = "Weak Network (RSSI below threshold)";

  public void evaluateAndPersist(List<ZoneComputationResult> zoneResults, Instant now, AlertRepository alertRepository) {
    for (ZoneComputationResult zone : zoneResults) {
      // Overcrowding
      boolean overcrowding = zone.intensity() > 0.85;
      persistOneCondition(
          alertRepository,
          zone.zoneName(),
          OVERCROWDING_MESSAGE,
          "High",
          overcrowding,
          zone.intensity(),
          zone.avgRssi(),
          now
      );

      // Weak network
      boolean weakNetwork = zone.avgRssi() < -80.0;
      persistOneCondition(
          alertRepository,
          zone.zoneName(),
          WEAK_NETWORK_MESSAGE,
          "Medium",
          weakNetwork,
          zone.intensity(),
          zone.avgRssi(),
          now
      );
    }
  }

  private void persistOneCondition(
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
        alertRepository.findByZoneAndMessageAndSeverityAndIsActiveTrue(zone, message, severity);

    // If previously active, mark inactive so the new alert is the current active one.
    if (!existingActive.isEmpty()) {
      for (AlertDocument doc : existingActive) {
        doc.setActive(false);
      }
      alertRepository.saveAll(existingActive);
    }

    if (!conditionActive) {
      return;
    }

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

  public AlertsResponse buildAlertsResponse(AlertRepository alertRepository) {
    List<AlertDocument> active = alertRepository.findByIsActiveTrueOrderByTimestampDesc();
    List<AlertDocument> history = alertRepository.findTop20ByOrderByTimestampDesc();

    List<AlertResponse> activeMapped = active.stream()
        .map(this::toResponse)
        .toList();

    List<AlertResponse> historyMapped = history.stream()
        .map(this::toResponse)
        .toList();

    List<AlertResponse> recent = historyMapped.stream().limit(10).toList();

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

