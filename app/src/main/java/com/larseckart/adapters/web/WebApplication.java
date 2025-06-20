package com.larseckart.adapters.web;

import com.larseckart.ApiKey;
import com.larseckart.core.domain.ConversationContext;
import com.larseckart.core.services.ConversationService;
import com.larseckart.core.services.ToolRegistry;
import com.larseckart.core.tools.ReadFileTool;
import com.larseckart.core.tools.ListFilesTool;
import com.larseckart.core.tools.EditFileTool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebApplication {

  public static void main(String[] args) {
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
    return new ConversationService(context, apiKey);
  }
}
