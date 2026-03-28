package com.crowdmonitoring.dashboard.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Central registry of all monitored zones and their
 * predefined GPS coordinates for heatmap rendering.
 *
 * No GPS hardware required — coordinates are fixed per zone.
 * Add more zones here as your venue grows.
 */
@Component  // Fix: was instantiated with "new" in multiple services
public class ZoneRegistry {

  public Map<String, double[]> getZoneCoordinates() {
    return Map.of(
            "Ground",   new double[]{28.6100, 77.2300},
            "Lobby",    new double[]{28.6120, 77.2320},
            "Hall A",   new double[]{28.6140, 77.2340},
            "Hall B",   new double[]{28.6160, 77.2360},
            "Parking",  new double[]{28.6080, 77.2280}
    );
  }

  public List<String> getZones() {
    return List.copyOf(getZoneCoordinates().keySet());
  }
}
