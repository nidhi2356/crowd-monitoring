package com.crowdmonitoring.dashboard.service;

public record WifiProbeSnapshot(String zone, int wifiCount, double avgRSSI) {}

