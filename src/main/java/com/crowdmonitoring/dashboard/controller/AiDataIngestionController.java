package com.crowdmonitoring.dashboard.controller;
import java.util.List;import com.crowdmonitoring.dashboard.model.dto.*;
import com.crowdmonitoring.dashboard.repository.AlertRepository;
import com.crowdmonitoring.dashboard.repository.CrowdMinuteDataRepository;
import com.crowdmonitoring.dashboard.repository.HourlyDataRepository;
import com.crowdmonitoring.dashboard.repository.SensorStatusRepository;
import com.crowdmonitoring.dashboard.service.AlertsEngineService;
import com.crowdmonitoring.dashboard.service.DashboardQueryService;
import com.crowdmonitoring.dashboard.service.HeatmapDataService;
import com.crowdmonitoring.dashboard.service.ZoneDataBuffer;
import com.crowdmonitoring.dashboard.websocket.RealTimeBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
@RestController
@RequestMapping("/api/ai")
public class AiDataIngestionController {

    private static final Logger log =
            LoggerFactory.getLogger(AiDataIngestionController.class);

    private final AlertsEngineService alertsEngineService;
    private final DashboardQueryService dashboardQueryService;
    private final HeatmapDataService heatmapDataService;
    private final RealTimeBroadcastService broadcastService;
    private final ZoneDataBuffer zoneDataBuffer;

    private final CrowdMinuteDataRepository crowdRepo;
    private final AlertRepository alertRepo;
    private final HourlyDataRepository hourlyRepo;
    private final SensorStatusRepository sensorStatusRepo;

    public AiDataIngestionController(
            AlertsEngineService alertsEngineService,
            DashboardQueryService dashboardQueryService,
            HeatmapDataService heatmapDataService,
            RealTimeBroadcastService broadcastService,
            ZoneDataBuffer zoneDataBuffer,
            CrowdMinuteDataRepository crowdRepo,
            AlertRepository alertRepo,
            HourlyDataRepository hourlyRepo,
            SensorStatusRepository sensorStatusRepo) {

        this.alertsEngineService = alertsEngineService;
        this.dashboardQueryService = dashboardQueryService;
        this.heatmapDataService = heatmapDataService;
        this.broadcastService = broadcastService;
        this.zoneDataBuffer = zoneDataBuffer;
        this.crowdRepo = crowdRepo;
        this.alertRepo = alertRepo;
        this.hourlyRepo = hourlyRepo;
        this.sensorStatusRepo = sensorStatusRepo;
    }

    @PostMapping("/data")
    public ResponseEntity<String> receiveAiData(
            @RequestBody List<AiSensorPayload> payloads) {

        if (payloads == null || payloads.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty payload");
        }

        try {
            double totalEntryRate = 0;
            double totalExitRate  = 0;

            for (AiSensorPayload payload : payloads) {

                log.info("Zone: {} Crowd: {} RSSI: {}",
                        payload.getZone(),
                        payload.getTotalCrowd(),
                        payload.getAvgRSSI());

                // ✅ store raw payload (no computed object)
                zoneDataBuffer.add(payload);

                // ✅ simple intensity calculation
                double intensity = payload.getTotalCrowd() / 100.0;

                // ✅ trigger alerts
                alertsEngineService.evaluateRealTime(
                        payload.getZone(),
                        intensity,
                        payload.getAvgRSSI(),
                        payload.getTimestamp() != null
                                ? payload.getTimestamp()
                                : Instant.now(),
                        alertRepo);

                totalEntryRate += 0 ;


                totalExitRate += 0 ;
            }

            DashboardResponse dashboardResponse =
                    dashboardQueryService.buildDashboardResponse(
                            crowdRepo,
                            alertRepo,
                            sensorStatusRepo,
                            hourlyRepo,
                            Instant.now());

            AlertsResponse alertsResponse =
                    alertsEngineService.buildAlertsResponse(alertRepo);

            // ✅ REAL-TIME heatmap
            HeatmapResponse heatmapResponse =
                    heatmapDataService.getRealtimeHeatmap();

            SensorStatusResponse sensorResponse =
                    SensorStatusResponse.builder()
                            .status("Operational")
                            .activeSensors((long) payloads.size() * 2)
                            .entryRate(totalEntryRate)
                            .exitRate(totalExitRate)
                            .build();

            broadcastService.broadcastDashboard(dashboardResponse);
            broadcastService.broadcastAlerts(alertsResponse);
            broadcastService.broadcastHeatmap(heatmapResponse);
            broadcastService.broadcastSensorStatus(sensorResponse);

            return ResponseEntity.ok("Processed successfully");

        } catch (Exception e) {
            log.error("Error processing AI data", e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }
}