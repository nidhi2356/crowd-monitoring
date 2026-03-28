package com.crowdmonitoring.dashboard.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.crowdmonitoring.dashboard.model.SystemInfoDocument;

public interface SystemInfoRepository
        extends MongoRepository<SystemInfoDocument, String> {

    Optional<SystemInfoDocument> findTopByOrderByUptimeStartAsc();
}