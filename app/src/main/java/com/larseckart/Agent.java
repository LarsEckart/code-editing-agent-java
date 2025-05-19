package com.larseckart;

import com.larseckart.tool.ToolDefinition;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

class Agent {

  private final Supplier<String> supplier;
  private final Client client;
  private final List<ToolDefinition> tools;

  public Agent(Supplier<String> supplier, Client client) {
    this(supplier, client, Collections.emptyList());
  }

  public Agent(Supplier<String> supplier, Client client, List<ToolDefinition> tools) {
    this.supplier = supplier;
    this.client = client;
    this.tools = tools;
  }

  public void start() {
    System.out.println("Chat with Claude (use 'ctrl-c' to quit)");

    boolean quit = false;
    boolean readUserInput = true;
    
    while (!quit) {
      String userInput = "";
      if (readUserInput) {
        System.out.print("\u001b[94mYou\u001b[0m: ");
        userInput = supplier.get();
        if (userInput.isEmpty()) {
          quit = true;
          continue;
        }
      }
      
      // Get response from Claude
      Client.Message message = client.send(userInput);
      
      // Process text response
      if (!message.getText().isEmpty()) {
        System.out.print("\u001b[95mClaude\u001b[0m: ");
        System.out.println("\u001b[92m" + message.getText() + "\u001b[0m");
      }
      
      // Process tool calls
      List<Client.ToolCall> toolCalls = message.getToolCalls();
      if (toolCalls.isEmpty()) {
        readUserInput = true;
        continue;
      }
      
      // Execute each tool call
      for (Client.ToolCall toolCall : toolCalls) {
        String toolName = toolCall.getName();
        String toolId = toolCall.getId();
        
        // Find the tool definition
        ToolDefinition toolDefinition = null;
        for (ToolDefinition tool : tools) {
          if (tool.getName().equals(toolName)) {
            toolDefinition = tool;
            break;
          }
        }
        
        if (toolDefinition == null) {
          System.out.println("\u001b[91mTool not found: " + toolName + "\u001b[0m");
          continue;
        }
        
        // Execute the tool
        System.out.println("\u001b[93mtool\u001b[0m: " + toolName + "(" + toolCall.getInput().toString() + ")");
        String toolResult = toolDefinition.execute(toolCall.getInput());
        
        // TODO: Send tool results back to Claude
        // This would require modifying the Client to handle tool responses
      }
      
      readUserInput = false;
    }
  }
}
