package com.crowdmonitoring.dashboard.service;

public record ZoneComputationResult(

        String zoneName,
        long totalCrowd,
        String density,
        double intensity,
        double networkScore,
        double riskScore,
        double entryRate,
        double exitRate,
        double avgRssi,
        int wifiCount,
        int cameraCount

) {}