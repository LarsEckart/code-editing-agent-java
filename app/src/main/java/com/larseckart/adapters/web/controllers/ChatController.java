package com.larseckart.adapters.web.controllers;

import com.larseckart.adapters.web.dto.ChatRequest;
import com.larseckart.adapters.web.dto.ChatResponse;
import com.larseckart.core.services.ConversationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
  private final ConversationService conversationService;

  public ChatController(ConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/message")
  public ChatResponse sendMessage(@Valid @RequestBody ChatRequest request) {
    logger.info("POST /api/chat/message called with message: {}", request.message());
    String response = conversationService.sendMessage(request.message());
    logger.info("Response generated with length: {} characters", response.length());
    return new ChatResponse(response);
  }
}
