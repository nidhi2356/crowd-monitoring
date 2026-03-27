package com.crowdmonitoring.dashboard.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.crowdmonitoring.dashboard.model.HeatmapDataPoint;

public interface HeatmapDataRepository extends MongoRepository<HeatmapDataPoint, String> {
  Optional<HeatmapDataPoint> findTopByOrderByTimestampDesc();

  List<HeatmapDataPoint> findByTimestampOrderByZoneAsc(Instant timestamp);
}

