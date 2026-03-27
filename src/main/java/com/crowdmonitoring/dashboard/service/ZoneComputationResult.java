package com.crowdmonitoring.dashboard.service;

public record ZoneComputationResult(
    String zoneName,
    long totalCrowd,
    String density,
    double intensity, // 0..1
    double networkScore, // 0..100
    double riskScore, // 0..100
    double entryRate, // people/min
    double exitRate, // people/min
    double avgRssi,
    int wifiCount,
    int cameraCount
) {}

