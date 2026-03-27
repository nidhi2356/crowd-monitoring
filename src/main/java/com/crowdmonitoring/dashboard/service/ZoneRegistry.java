package com.crowdmonitoring.dashboard.service;

import java.util.List;
import java.util.Map;

public class ZoneRegistry {
  // Predefined heatmap coordinates for each zone.
  public Map<String, double[]> getZoneCoordinates() {
    // Ground: lat 28.6100, lng 77.2300
    return Map.of("Ground", new double[] {28.61, 77.23});
  }

  public List<String> getZones() {
    return List.copyOf(getZoneCoordinates().keySet());
  }
}

