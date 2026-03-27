package com.crowdmonitoring.dashboard.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.crowdmonitoring.dashboard.model.HourlyData;

public interface HourlyDataRepository extends MongoRepository<HourlyData, String> {
  Optional<HourlyData> findByHourStart(Instant hourStart);

  List<HourlyData> findByHourStartBetweenOrderByHourStartAsc(Instant start, Instant end);
}

