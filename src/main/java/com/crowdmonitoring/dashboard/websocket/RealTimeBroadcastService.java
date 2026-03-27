package com.crowdmonitoring.dashboard.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.crowdmonitoring.dashboard.model.dto.AlertsResponse;
import com.crowdmonitoring.dashboard.model.dto.DashboardResponse;
import com.crowdmonitoring.dashboard.model.dto.HeatmapResponse;

@Service
public class RealTimeBroadcastService {

  private final SimpMessagingTemplate messagingTemplate;

  public RealTimeBroadcastService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void broadcastDashboard(DashboardResponse response) {
    messagingTemplate.convertAndSend("/topic/crowd", response);
  }

  public void broadcastAlerts(AlertsResponse response) {
    messagingTemplate.convertAndSend("/topic/alerts", response);
  }

  public void broadcastHeatmap(HeatmapResponse response) {
    messagingTemplate.convertAndSend("/topic/heatmap", response);
  }
}

