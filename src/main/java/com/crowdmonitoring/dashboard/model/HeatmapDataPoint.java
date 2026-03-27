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
@Document(collection = "heatmap_data")
public class HeatmapDataPoint {
  @Id
  private String id;

  private String zone;
  private Instant timestamp;

  private double lat;
  private double lng;
  private double intensity; // 0..1
}

