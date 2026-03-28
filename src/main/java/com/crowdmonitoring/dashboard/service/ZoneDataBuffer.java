package com.crowdmonitoring.dashboard.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.crowdmonitoring.dashboard.model.dto.AiSensorPayload;

@Component
public class ZoneDataBuffer {

    private final ConcurrentHashMap<String, List<AiSensorPayload>>
            buffer = new ConcurrentHashMap<>();

    /**
     * Store incoming AI payload
     */
    public void add(AiSensorPayload payload) {
        buffer.computeIfAbsent(
                payload.getZone(),
                k -> new ArrayList<>()
        ).add(payload);
    }

    /**
     * Drain buffer every 60s
     */
    public Map<String, List<AiSensorPayload>> drainAndClear() {
        Map<String, List<AiSensorPayload>> snapshot =
                new HashMap<>(buffer);
        buffer.clear();
        return snapshot;
    }

    /**
     * Get latest per zone (for real-time)
     */
    public Map<String, AiSensorPayload> getLatestPerZone() {
        Map<String, AiSensorPayload> latest = new HashMap<>();

        buffer.forEach((zone, list) -> {
            if (!list.isEmpty()) {
                latest.put(zone, list.get(list.size() - 1));
            }
        });

        return latest;
    }
}