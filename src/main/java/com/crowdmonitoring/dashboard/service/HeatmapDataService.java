package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.dto.AiSensorPayload;
import com.crowdmonitoring.dashboard.model.dto.HeatmapPointResponse;
import com.crowdmonitoring.dashboard.model.dto.HeatmapResponse;

@Service
public class HeatmapDataService {

  private final ZoneDataBuffer zoneDataBuffer;
  private final ZoneRegistry zoneRegistry;

  public HeatmapDataService(
          ZoneDataBuffer zoneDataBuffer,
          ZoneRegistry zoneRegistry) {
    this.zoneDataBuffer = zoneDataBuffer;
    this.zoneRegistry = zoneRegistry;
  }

  public HeatmapResponse getRealtimeHeatmap() {

    // Get latest AI data per zone
    Map<String, AiSensorPayload> latest =
            zoneDataBuffer.getLatestPerZone();

    Map<String, double[]> coords =
            zoneRegistry.getZoneCoordinates();

    List<HeatmapPointResponse> points = latest.entrySet()
            .stream()
            .map(entry -> {
              String zone = entry.getKey();
              AiSensorPayload payload = entry.getValue();

              if (zone == null || payload == null) return null;

              double[] latLng = coords.get(zone);
              if (latLng == null) return null;

              return HeatmapPointResponse.builder()
                      .zone(zone)
                      .lat(latLng[0])
                      .lng(latLng[1])
                      .intensity(payload.getIntensity())
                      .timestamp(Instant.now())
                      .build();
            })
            .filter(p -> p != null)
            .toList();

    return HeatmapResponse.builder()
            .points(points)
            .build();
  }
}