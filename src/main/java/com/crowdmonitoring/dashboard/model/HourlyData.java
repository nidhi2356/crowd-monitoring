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
@Document(collection = "hourly_data")
public class HourlyData {
  @Id
  private String id;

  // Beginning of the hour (UTC).
  private Instant hourStart;

  private long peakCrowd;
  private double sumTraffic;
  private long countMinutes;
  private double averageTraffic;

  private Instant updatedAt;
}

