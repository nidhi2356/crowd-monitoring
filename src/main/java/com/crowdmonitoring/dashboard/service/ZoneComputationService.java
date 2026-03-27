package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.time.ZoneId;

import com.crowdmonitoring.dashboard.model.CrowdMinuteData;

import org.springframework.stereotype.Service;

@Service
public class ZoneComputationService {

  private static double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }

  private static String densityFromCrowd(long crowd) {
    if (crowd < 50) return "Low";
    if (crowd <= 150) return "Medium";
    return "High";
  }

  public ZoneComputationResult compute(
      String zone,
      CameraSnapshot cameraSnapshot,
      WifiProbeSnapshot wifiSnapshot,
      CrowdMinuteData previous
  ) {
    long prevCrowd = previous == null ? 0L : previous.getTotalCrowd();

    // Synthetic but stable crowd model:
    // - cameraCount and wifiCount influence base crowd estimate
    // - a time-wave simulates varying traffic intensity through the day
    // - deterministic "noise" avoids fully random dashboards.
    Instant now = Instant.now();
    ZoneId zoneId = ZoneId.of("Asia/Kolkata");
    long minuteOfDay = now.atZone(zoneId).getHour() * 60L + now.atZone(zoneId).getMinute();
    double wave = 0.5 + 0.5 * Math.sin(minuteOfDay / 30.0); // 0..1
    double dailyFactor = 0.85 + wave * 0.6; // 0.85..1.45

    double baseFromCameras = cameraSnapshot.cameraCount() * 2.15;
    double baseFromWifi = wifiSnapshot.wifiCount() * 1.05;
    double crowdRaw = (0.55 * baseFromCameras + 0.45 * baseFromWifi) * dailyFactor;

    // Deterministic wobble using RSSI magnitude.
    double wobble = 1.0 + clamp((wifiSnapshot.avgRSSI() + 80) / 40.0, -0.25, 0.25);
    long currentCrowd = Math.max(0L, Math.round(crowdRaw * wobble));

    double intensity = clamp(currentCrowd / 250.0, 0.0, 1.0);
    String density = densityFromCrowd(currentCrowd);

    // RSSI -> network quality score (0..1): RSSI=-90 => 0, RSSI=-60 => 1.
    double network01 = clamp((wifiSnapshot.avgRSSI() + 90.0) / 30.0, 0.0, 1.0);
    double networkScore = network01 * 100.0;

    // Risk combines crowd intensity and weak-network impact.
    double riskScore = 0.65 * (intensity * 100.0) + 0.35 * ((1.0 - network01) * 100.0);
    riskScore = clamp(riskScore, 0.0, 100.0);

    double entryRate = Math.max(0, currentCrowd - prevCrowd); // people/min
    double exitRate = Math.max(0, prevCrowd - currentCrowd); // people/min

    return new ZoneComputationResult(
        zone,
        currentCrowd,
        density,
        intensity,
        networkScore,
        riskScore,
        entryRate,
        exitRate,
        wifiSnapshot.avgRSSI(),
        wifiSnapshot.wifiCount(),
        cameraSnapshot.cameraCount()
    );
  }
}

