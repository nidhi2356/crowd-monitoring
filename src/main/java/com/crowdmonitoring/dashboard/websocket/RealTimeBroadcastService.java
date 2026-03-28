package com.crowdmonitoring.dashboard.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.dto.AlertsResponse;
import com.crowdmonitoring.dashboard.model.dto.DashboardResponse;
import com.crowdmonitoring.dashboard.model.dto.HeatmapResponse;
import com.crowdmonitoring.dashboard.model.dto.SensorStatusResponse;

@Service
public class RealTimeBroadcastService {

  private final SimpMessagingTemplate messagingTemplate;

  public RealTimeBroadcastService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /** Frontend subscribes to: /topic/crowd */
  public void broadcastDashboard(DashboardResponse response) {
    messagingTemplate.convertAndSend("/topic/crowd", response);
  }

  /** Frontend subscribes to: /topic/alerts */
  public void broadcastAlerts(AlertsResponse response) {
    messagingTemplate.convertAndSend("/topic/alerts", response);
  }

  /** Frontend subscribes to: /topic/heatmap */
  public void broadcastHeatmap(HeatmapResponse response) {
    messagingTemplate.convertAndSend("/topic/heatmap", response);
  }

  /**
   * NEW — Frontend subscribes to: /topic/sensors
   * Was missing from original code.
   */
  public void broadcastSensorStatus(SensorStatusResponse response) {
    messagingTemplate.convertAndSend("/topic/sensors", response);
  }
}