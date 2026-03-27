// package com.crowdmonitoring.dashboard.repository;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.mongodb.repository.MongoRepository;

// import com.crowdmonitoring.dashboard.model.DailySummary;

// public interface DailySummaryRepository extends MongoRepository<DailySummary, String> {
//   Optional<DailySummary> findByDate(LocalDate date);

//   List<DailySummary> findByDateBetween(LocalDate start, LocalDate end);

//   Optional<DailySummary> findTopByOrderByAverageTrafficDesc();
// }

package com.crowdmonitoring.dashboard.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.crowdmonitoring.dashboard.model.DailySummary;

public interface DailySummaryRepository extends MongoRepository<DailySummary, String> {
  Optional<DailySummary> findByDate(LocalDate date);

  List<DailySummary> findByDateBetween(LocalDate start, LocalDate end);

  Optional<DailySummary> findTopByOrderByAverageTrafficDesc();
}