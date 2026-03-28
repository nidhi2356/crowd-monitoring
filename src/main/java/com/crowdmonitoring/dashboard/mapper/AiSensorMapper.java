package com.crowdmonitoring.dashboard.mapper;

import com.crowdmonitoring.dashboard.model.CrowdMinuteData;
import com.crowdmonitoring.dashboard.model.dto.AiSensorPayload;
import com.crowdmonitoring.dashboard.service.ZoneComputationResult;

import org.springframework.stereotype.Component;

@Component
public class AiSensorMapper {

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public ZoneComputationResult toComputationResult(
            AiSensorPayload payload,
            CrowdMinuteData previous) {

        long totalCrowd = payload.getTotalCrowd();

        // Density
        String density;
        if (totalCrowd < 50) {
            density = "Low";
        } else if (totalCrowd <= 150) {
            density = "Medium";
        } else {
            density = "High";
        }

        // Intensity (0–1)
        double intensity = clamp(totalCrowd / 250.0, 0.0, 1.0);

        // Network Score (0–100)
        double networkScore = clamp(
                (payload.getAvgRSSI() + 90.0) / 30.0 * 100.0,
                0.0, 100.0);

        // Risk Score
        double riskScore = clamp(
                (0.65 * intensity * 100.0) +
                        (0.35 * (100.0 - networkScore)),
                0.0, 100.0);

        // Entry / Exit Rate
        long prevCrowd = (previous == null)
                ? 0L : previous.getTotalCrowd();

        double entryRate = Math.max(0, totalCrowd - prevCrowd);
        double exitRate  = Math.max(0, prevCrowd - totalCrowd);

        return new ZoneComputationResult(
                payload.getZone(),
                totalCrowd,
                density,
                intensity,
                networkScore,
                riskScore,
                entryRate,
                exitRate,
                payload.getAvgRSSI(),
                payload.getWifiCount(),
                payload.getCameraCount()
        );
    }

    public CrowdMinuteData toCrowdMinuteData(
            AiSensorPayload payload,
            ZoneComputationResult computed) {

        return CrowdMinuteData.builder()
                .zone(payload.getZone())
                .timestamp(payload.getTimestamp())
                .totalCrowd(computed.totalCrowd())
                .density(computed.density())
                .intensity(computed.intensity())
                .networkScore(computed.networkScore())
                .riskScore(computed.riskScore())
                .entryRate(computed.entryRate())
                .exitRate(computed.exitRate())
                .avgRssi(computed.avgRssi())
                .wifiCount(computed.wifiCount())
                .cameraCount(computed.cameraCount())
                .build();
    }
}