package com.larseckart.adapters.cli;

import com.larseckart.ApiKey;
import com.larseckart.core.domain.ConversationContext;
import com.larseckart.core.ports.input.InputPort;
import com.larseckart.core.ports.output.OutputPort;
import com.larseckart.core.services.ChatService;
import com.larseckart.core.services.ConversationService;
import com.larseckart.core.services.ToolRegistry;
import com.larseckart.core.tools.EditFileTool;
import com.larseckart.core.tools.ListFilesTool;
import com.larseckart.core.tools.ReadFileTool;

public class CliApplication {

  public static void main(String[] args) {
    InputPort inputPort = new ConsoleInputAdapter();
    OutputPort outputPort = new ConsoleOutputAdapter();

    ConversationContext context = new ConversationContext();
    ApiKey apiKey = ApiKey.fromEnvironment("code_editing_agent_api_key");

    ToolRegistry toolRegistry = new ToolRegistry();
    toolRegistry.registerTool(new ReadFileTool());
    toolRegistry.registerTool(new ListFilesTool());
    toolRegistry.registerTool(new EditFileTool());

    ConversationService conversationService =
        new ConversationService(context, apiKey, toolRegistry);

    ChatService chatService = new ChatService(inputPort, outputPort, conversationService);
    chatService.startChat();
  }
}
