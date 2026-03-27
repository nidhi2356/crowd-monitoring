package com.crowdmonitoring.dashboard.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.crowdmonitoring.dashboard.model.SensorStatusDocument;

public interface SensorStatusRepository extends MongoRepository<SensorStatusDocument, String> {
  Optional<SensorStatusDocument> findTopByOrderByTimestampDesc();
}

