package com.larseckart.core.domain.ai;

import com.larseckart.core.domain.ChatMessage;
import java.util.List;

public record AIRequest(
    List<ChatMessage> messages,
    String systemPrompt,
    List<AITool> tools,
    int maxTokens) {}
