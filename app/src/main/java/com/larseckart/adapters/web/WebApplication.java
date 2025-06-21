package com.larseckart.adapters.web;

import com.larseckart.ApiKey;
import com.larseckart.adapters.ai.AnthropicProvider;
import com.larseckart.core.domain.ConversationContext;
import com.larseckart.core.services.ConversationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebApplication {

  public static void main(String[] args) {
    System.setProperty("app.mode", "web");
    SpringApplication.run(WebApplication.class, args);
  }

  @Bean
  public ConversationContext conversationContext() {
    return new ConversationContext();
  }

  @Bean
  public ApiKey apiKey() {
    return ApiKey.fromEnvironment("code_editing_agent_api_key");
  }

  @Bean
  public ConversationService conversationService(ConversationContext context, ApiKey apiKey) {
    return new ConversationService(context, new AnthropicProvider(apiKey));
  }
}
