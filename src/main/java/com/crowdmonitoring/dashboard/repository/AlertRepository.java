package com.crowdmonitoring.dashboard.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.crowdmonitoring.dashboard.model.AlertDocument;

public interface AlertRepository extends MongoRepository<AlertDocument, String> {

  List<AlertDocument> findByIsActiveTrueOrderByTimestampDesc();

  List<AlertDocument> findTop20ByOrderByTimestampDesc();

  List<AlertDocument> findByZoneAndMessageAndSeverityAndIsActiveTrue(
      String zone,
      String message,
      String severity);

  List<AlertDocument> findByTimestampAfterOrderByTimestampDesc(Instant start);

  Optional<AlertDocument> findTopByOrderByTimestampDesc();
}

