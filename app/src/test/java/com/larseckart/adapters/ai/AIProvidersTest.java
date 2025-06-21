package com.larseckart.adapters.ai;

import static org.assertj.core.api.Assertions.*;

import com.larseckart.ApiKey;
import com.larseckart.core.domain.ChatMessage;
import com.larseckart.core.domain.ai.AIRequest;
import com.larseckart.core.domain.ai.AIResponse;
import com.larseckart.core.ports.AIProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AIProvidersTest {

  @Test
  void should_create_anthropic_provider() {
    ApiKey apiKey = ApiKey.forTesting("test-key");
    AIProvider provider = new AnthropicProvider(apiKey);

    assertThat(provider).isNotNull();
    assertThat(provider.getProviderName()).isEqualTo("Anthropic Claude");
  }

  @Test
  void should_create_gemini_provider() {
    ApiKey apiKey = ApiKey.forTesting("test-key");
    AIProvider provider = new GeminiProvider(apiKey);

    assertThat(provider).isNotNull();
    assertThat(provider.getProviderName()).isEqualTo("Google Gemini");
  }

  @Test
  void should_create_providers_from_factory() {
    ApiKey apiKey = ApiKey.forTesting("test-key");

    AIProvider anthropicProvider =
        AIProviderFactory.create(AIProviderFactory.ProviderType.ANTHROPIC, apiKey);
    assertThat(anthropicProvider).isInstanceOf(AnthropicProvider.class);

    AIProvider geminiProvider =
        AIProviderFactory.create(AIProviderFactory.ProviderType.GEMINI, apiKey);
    assertThat(geminiProvider).isInstanceOf(GeminiProvider.class);
  }

  @Test
  void should_handle_simple_ai_request_structure() {
    // This test just verifies that the AIRequest/AIResponse structure works
    // without making actual API calls

    List<ChatMessage> messages = List.of(ChatMessage.user("Hello"));
    AIRequest request = new AIRequest(messages, "Be helpful", null, 100);

    assertThat(request.messages()).hasSize(1);
    assertThat(request.systemPrompt()).isEqualTo("Be helpful");
    assertThat(request.maxTokens()).isEqualTo(100);

    AIResponse response = new AIResponse("Hi there!", List.of(), false);
    assertThat(response.textContent()).isEqualTo("Hi there!");
    assertThat(response.hasToolUse()).isFalse();
  }
}
