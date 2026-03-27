package com.crowdmonitoring.dashboard.model;

import java.time.LocalDate;

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
@Document(collection = "daily_summary")
public class DailySummary {
  @Id
  private String id;

  private LocalDate date;

  private long peakCrowd;
  private double sumTraffic;
  private long countMinutes;
  private double averageTraffic;

  private java.time.Instant updatedAt;
}

