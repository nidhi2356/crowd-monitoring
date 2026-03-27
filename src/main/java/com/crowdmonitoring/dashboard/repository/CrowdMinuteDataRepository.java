package com.crowdmonitoring.dashboard.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.crowdmonitoring.dashboard.model.CrowdMinuteData;

public interface CrowdMinuteDataRepository extends MongoRepository<CrowdMinuteData, String> {
  Optional<CrowdMinuteData> findTopByZoneOrderByTimestampDesc(String zone);

  List<CrowdMinuteData> findByTimestampBetween(Instant start, Instant end);
}

