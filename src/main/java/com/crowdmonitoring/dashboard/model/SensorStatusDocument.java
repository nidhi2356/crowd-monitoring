package com.crowdmonitoring.dashboard.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sensor_status")
public class SensorStatusDocument {
  @Id
  private String id;

  private Instant timestamp;
  private String status; // Operational | Degraded | Offline
  private long activeSensors;
  private long uptimeSeconds;

  // Overall traffic dynamics used by the dashboard.
  private double entryRate; // people/min
  private double exitRate; // people/min
}

