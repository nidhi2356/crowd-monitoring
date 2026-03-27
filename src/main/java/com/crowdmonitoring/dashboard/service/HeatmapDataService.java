package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.HeatmapDataPoint;
import com.crowdmonitoring.dashboard.model.dto.HeatmapPointResponse;
import com.crowdmonitoring.dashboard.model.dto.HeatmapResponse;
import com.crowdmonitoring.dashboard.repository.HeatmapDataRepository;

@Service
public class HeatmapDataService {

  private final ZoneRegistry zoneRegistry = new ZoneRegistry();

  public void persistHeatmap(
      List<ZoneComputationResult> zoneResults,
      Instant now,
      HeatmapDataRepository heatmapRepository
  ) {
    Map<String, double[]> coords = zoneRegistry.getZoneCoordinates();
    List<HeatmapDataPoint> toSave = new ArrayList<>();

    for (ZoneComputationResult z : zoneResults) {
      double[] latLng = coords.get(z.zoneName());
      if (latLng == null) continue;

      HeatmapDataPoint point = HeatmapDataPoint.builder()
          .zone(z.zoneName())
          .timestamp(now)
          .lat(latLng[0])
          .lng(latLng[1])
          .intensity(z.intensity())
          .build();

      toSave.add(point);
    }

    if (!toSave.isEmpty()) {
      heatmapRepository.saveAll(toSave);
    }
  }

  public HeatmapResponse getLatestHeatmap(HeatmapDataRepository heatmapRepository) {
    Instant latest = heatmapRepository.findTopByOrderByTimestampDesc()
        .map(HeatmapDataPoint::getTimestamp)
        .orElse(null);

    if (latest == null) {
      return HeatmapResponse.builder().points(List.of()).build();
    }

    List<HeatmapDataPoint> points = heatmapRepository.findByTimestampOrderByZoneAsc(latest);
    List<HeatmapPointResponse> mapped = points.stream()
        .map(p -> HeatmapPointResponse.builder()
            .lat(p.getLat())
            .lng(p.getLng())
            .intensity(p.getIntensity())
            .build())
        .toList();

    return HeatmapResponse.builder().points(mapped).build();
  }
}

