package com.crowdmonitoring.dashboard.model.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Exactly matches the JSON structure AI sends.
 * DO NOT add or remove fields.
 *
 * AI sends:
 * {
 *   "zone": "Ground A",
 *   "cameraCount": 10,
 *   "wifiCount": 39,
 *   "totalCrowd": 49,
 *   "avgRSSI": -68,
 *   "timestamp": "2026-03-28T03:43:51.928630"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSensorPayload {

    private String zone;        // zone name from AI
    private int cameraCount;    // people count from CCTV
    private int wifiCount;      // device count from WiFi probe
    private int totalCrowd;     // AI merged crowd count
    private int avgRSSI;        // WiFi signal strength (dBm)
    private Instant timestamp;  // AI generated timestamp
}