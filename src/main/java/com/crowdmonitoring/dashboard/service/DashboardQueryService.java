package com.crowdmonitoring.dashboard.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.CrowdMinuteData;
import com.crowdmonitoring.dashboard.model.HourlyData;
import com.crowdmonitoring.dashboard.model.SensorStatusDocument;
import com.crowdmonitoring.dashboard.model.dto.DashboardResponse;
import com.crowdmonitoring.dashboard.model.dto.SensorStatusResponse;
import com.crowdmonitoring.dashboard.model.dto.ZoneStatsResponse;
import com.crowdmonitoring.dashboard.repository.AlertRepository;
import com.crowdmonitoring.dashboard.repository.CrowdMinuteDataRepository;
import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;
import com.crowdmonitoring.dashboard.repository.SensorStatusRepository;

@Service
public class DashboardQueryService {

  private final ZoneRegistry zoneRegistry = new ZoneRegistry();

  public DashboardResponse buildDashboardResponse(
      CrowdMinuteDataRepository crowdRepo,
      AlertRepository alertRepository,
      SensorStatusRepository sensorStatusRepository,
      HourlyDataRepository hourlyRepo,
      Instant now
  ) {
    List<String> zones = zoneRegistry.getZones();

    List<CrowdMinuteData> latestPerZone = zones.stream()
        .map(z -> crowdRepo.findTopByZoneOrderByTimestampDesc(z).orElse(null))
        .filter(x -> x != null)
        .toList();

    long totalCrowd = latestPerZone.stream().mapToLong(CrowdMinuteData::getTotalCrowd).sum();
    double networkScore = latestPerZone.isEmpty() ? 0.0 :
        latestPerZone.stream().mapToDouble(CrowdMinuteData::getNetworkScore).average().orElse(0.0);

    List<ZoneStatsResponse> zoneStats = latestPerZone.stream()
        .map(this::toZoneStats)
        .toList();

    // Peak crowd from hourly buckets in last 24h.
    Instant start = startOfHour(now.minus(23, ChronoUnit.HOURS));
    Instant end = start.plus(24, ChronoUnit.HOURS);
    List<HourlyData> hourly = hourlyRepo.findByHourStartBetweenOrderByHourStartAsc(start, end);
    long peakCrowd = hourly.stream().mapToLong(HourlyData::getPeakCrowd).max().orElse(0L);

    SensorStatusDocument sensorDoc = sensorStatusRepository.findTopByOrderByTimestampDesc().orElse(null);
    SensorStatusResponse sensorStatus = sensorDoc == null ? SensorStatusResponse.builder()
        .status("Unknown")
        .activeSensors(0)
        .uptimeSeconds(0)
        .entryRate(0)
        .exitRate(0)
        .build() : SensorStatusResponse.builder()
        .status(sensorDoc.getStatus())
        .activeSensors(sensorDoc.getActiveSensors())
        .uptimeSeconds(sensorDoc.getUptimeSeconds())
        .entryRate(sensorDoc.getEntryRate())
        .exitRate(sensorDoc.getExitRate())
        .build();

    return DashboardResponse.builder()
        .totalCrowd(totalCrowd)
        .networkScore(networkScore)
        .peakCrowd(peakCrowd)
        .zones(zoneStats)
        .sensorStatus(sensorStatus)
        .build();
  }

  private ZoneStatsResponse toZoneStats(CrowdMinuteData d) {
    return ZoneStatsResponse.builder()
        .zoneName(d.getZone())
        .crowdCount(d.getTotalCrowd())
        .density(d.getDensity())
        .riskScore(d.getRiskScore())
        .entryRate(d.getEntryRate())
        .exitRate(d.getExitRate())
        .build();
  }

  private Instant startOfHour(Instant instant) {
    ZoneId zoneId = ZoneId.of("Asia/Kolkata");
    ZonedDateTime zdt = instant.atZone(zoneId).truncatedTo(ChronoUnit.HOURS);
    return zdt.toInstant();
  }
}

