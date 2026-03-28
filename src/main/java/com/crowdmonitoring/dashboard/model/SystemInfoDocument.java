package com.crowdmonitoring.dashboard.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Stores system uptime information.
 * Only one document exists — upserted on startup.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "system_info")
public class SystemInfoDocument {

    @Id
    private String id;

    private Instant uptimeStart;   // When app first started
    private Instant lastRestart;   // Last restart time
}