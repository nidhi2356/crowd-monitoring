package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SensorDataService {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${crowd.external.mockMode:true}")
  private boolean mockMode;

  @Value("${crowd.external.cameraApiUrl:}")
  private String cameraApiUrl;

  @Value("${crowd.external.wifiProbeApiUrl:}")
  private String wifiProbeApiUrl;

  private static double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }

  private long minuteOfDay() {
    Instant now = Instant.now();
    ZoneId zoneId = ZoneId.of("Asia/Kolkata");
    return now.atZone(zoneId).getHour() * 60L + now.atZone(zoneId).getMinute();
  }

  public CameraSnapshot fetchCameraSnapshot(String zone) {
    if (!mockMode && cameraApiUrl != null && !cameraApiUrl.isBlank()) {
      // Expected payload: { "zone": "Ground", "cameraCount": 50 }
      ResponseEntity<CameraSnapshot> res =
          restTemplate.getForEntity(cameraApiUrl + "?zone=" + zone, CameraSnapshot.class);
      return res.getBody();
    }

    // Mock: cameraCount in a realistic range with daily waves.
    long m = minuteOfDay();
    double wave = 0.5 + 0.5 * Math.sin(m / 18.0 + zone.hashCode() * 0.001);
    int cameraCount = (int) Math.round(35 + wave * 35); // ~35..70
    return new CameraSnapshot(zone, cameraCount);
  }

  public WifiProbeSnapshot fetchWifiProbeSnapshot(String zone) {
    if (!mockMode && wifiProbeApiUrl != null && !wifiProbeApiUrl.isBlank()) {
      // Expected payload: { "zone": "Ground", "wifiCount": 65, "avgRSSI": -70 }
      ResponseEntity<WifiProbeSnapshot> res =
          restTemplate.getForEntity(wifiProbeApiUrl + "?zone=" + zone, WifiProbeSnapshot.class);
      return res.getBody();
    }

    // Mock: wifiCount correlated with crowd wave and RSSI quality fluctuates.
    long m = minuteOfDay();
    double wave = 0.5 + 0.5 * Math.sin(m / 22.0 + zone.hashCode() * 0.001);

    int wifiCount = (int) Math.round(40 + wave * 40); // ~40..80

    // Make weak network occasionally appear (RSSI < -80).
    double rssiBase = -68.0 - wave * 22.0; // ~ -90..-68
    double rssiWobble = clamp((Math.cos(m / 11.0) * 2.0), -3, 3);
    double avgRSSI = clamp(rssiBase + rssiWobble, -92, -55);

    return new WifiProbeSnapshot(zone, wifiCount, avgRSSI);
  }
}

