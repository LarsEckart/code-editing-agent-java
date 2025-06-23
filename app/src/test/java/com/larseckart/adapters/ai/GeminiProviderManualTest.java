package com.larseckart.adapters.ai;

import com.larseckart.ApiKey;
import com.larseckart.core.domain.ChatMessage;
import com.larseckart.core.domain.ai.AIRequest;
import com.larseckart.core.domain.ai.AIResponse;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Manual test for Gemini provider with tool calling. Enable by removing @Disabled and setting
 * GOOGLE_API_KEY environment variable.
 */
@Disabled("Manual test - requires GOOGLE_API_KEY")
class GeminiProviderManualTest {

  @Test
  void test_list_files_tool() {
    // This test demonstrates how the Gemini provider works with tool calling
    String apiKey = System.getenv("GOOGLE_API_KEY");
    if (apiKey == null) {
      System.out.println("GOOGLE_API_KEY not set, skipping test");
      return;
    }

    GeminiProvider provider = new GeminiProvider(ApiKey.forTesting(apiKey));

    // Create a request that should trigger the list files tool
    List<ChatMessage> messages =
        List.of(ChatMessage.user("Can you list the files in the current directory?"));

    AIRequest request =
        new AIRequest(
            messages,
            "You are a helpful assistant with access to file system tools.",
            null, // Tools are registered automatically in GeminiProvider
            1024);

    try {
      AIResponse response = provider.sendMessage(request);
      System.out.println("Gemini response:");
      System.out.println(response.textContent());

      // The response should include the results of the listFiles tool call
      // Gemini will automatically execute the tool and incorporate the results
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
