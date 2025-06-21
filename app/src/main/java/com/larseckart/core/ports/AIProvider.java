package com.larseckart.core.ports;

import com.larseckart.core.domain.ai.AIRequest;
import com.larseckart.core.domain.ai.AIResponse;

public interface AIProvider {
  
  AIResponse sendMessage(AIRequest request);
  
  String getProviderName();
}
