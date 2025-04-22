package com.larseckart;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class ClientTest {

  private MockWebServer mockWebServer;
  private Context context;
  private Client client;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    context = new Context();
    client = new Client(context,
        mockWebServer.url("/v1beta/models/gemini-2.0-flash:generateContent").toString());
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void testSend_appendsAnswerToContext_andVerifiesRequest() throws Exception {
    String mockResponse = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  { "text": "This is a test answer." }
                ]
              }
            }
          ]
        }
        """;
    mockWebServer.enqueue(
        new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json"));

    client.send("Hello?");

    // Verify context updated
    List<String> history = context.getHistory();
    assertEquals(2, history.size());
    assertEquals("Hello?", history.get(0));
    assertEquals("This is a test answer.", history.get(1));

    // Verify request
    var recordedRequest = mockWebServer.takeRequest();
    assertEquals("POST", recordedRequest.getMethod());
    assertEquals("/v1beta/models/gemini-2.0-flash:generateContent", recordedRequest.getPath());
    String requestBody = recordedRequest.getBody().readUtf8();
    assertTrue(requestBody.contains("Hello?"), "Request body should contain the user input");
    assertTrue(requestBody.contains("parts"), "Request body should contain 'parts'");
  }
}
