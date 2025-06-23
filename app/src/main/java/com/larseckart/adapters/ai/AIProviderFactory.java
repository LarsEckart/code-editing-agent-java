package com.larseckart.adapters.ai;

import static org.slf4j.LoggerFactory.getLogger;

import com.larseckart.ApiKey;
import com.larseckart.core.ports.AIProvider;
import org.slf4j.Logger;

public class AIProviderFactory {

  private static final Logger log = getLogger(AIProviderFactory.class);

  public enum ProviderType {
    ANTHROPIC,
    GEMINI
  }

  public static AIProvider create(ProviderType type, ApiKey apiKey) {
    log.info("Creating AI provider of type: {}", type);
    return switch (type) {
      case ANTHROPIC -> new AnthropicProvider(apiKey);
      case GEMINI -> new GeminiProvider(apiKey);
    };
  }

  public static AIProvider createFromEnvironment() {
    String providerType = System.getenv("AI_PROVIDER");
    log.info("AI_PROVIDER environment variable: {}", providerType);

    ProviderType type = ProviderType.ANTHROPIC; // default

    if ("gemini".equalsIgnoreCase(providerType)) {
      type = ProviderType.GEMINI;
    }

    log.info("Selected provider type: {}", type);

    ApiKey apiKey =
        switch (type) {
          case ANTHROPIC -> ApiKey.fromEnvironment("code_editing_agent_api_key");
          case GEMINI -> ApiKey.fromEnvironment("GOOGLE_API_KEY");
        };

    return create(type, apiKey);
  }
}
