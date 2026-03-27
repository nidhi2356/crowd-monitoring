package com.crowdmonitoring.dashboard.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.crowdmonitoring.dashboard.model.dto.ChatbotRequest;
import com.crowdmonitoring.dashboard.model.dto.ChatbotResponse;
import com.crowdmonitoring.dashboard.service.ChatbotService;

@RestController
@Validated
public class ChatbotController {

  private final ChatbotService chatbotService;

  public ChatbotController(ChatbotService chatbotService) {
    this.chatbotService = chatbotService;
  }

  @PostMapping("/chatbot")
  public ChatbotResponse chatbot(@Valid @RequestBody ChatbotRequest request) {
    return chatbotService.getReply(request.getMessage());
  }
}

