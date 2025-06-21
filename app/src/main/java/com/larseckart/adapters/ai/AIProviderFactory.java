package com.larseckart.adapters.ai;

import com.larseckart.ApiKey;
import com.larseckart.core.ports.AIProvider;

public class AIProviderFactory {

  public enum ProviderType {
    ANTHROPIC,
    GEMINI
  }

  public static AIProvider create(ProviderType type, ApiKey apiKey) {
    return switch (type) {
      case ANTHROPIC -> new AnthropicProvider(apiKey);
      case GEMINI -> new GeminiProvider(apiKey);
    };
  }

  public static AIProvider createFromEnvironment() {
    String providerType = System.getenv("AI_PROVIDER");
    ProviderType type = ProviderType.ANTHROPIC; // default

    if ("gemini".equalsIgnoreCase(providerType)) {
      type = ProviderType.GEMINI;
    }

    ApiKey apiKey =
        switch (type) {
          case ANTHROPIC -> ApiKey.fromEnvironment("code_editing_agent_api_key");
          case GEMINI -> ApiKey.fromEnvironment("GOOGLE_API_KEY");
        };

    return create(type, apiKey);
  }
}
