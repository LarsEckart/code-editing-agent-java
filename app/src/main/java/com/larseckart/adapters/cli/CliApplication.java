package com.larseckart.adapters.cli;

import com.larseckart.adapters.ai.AIProviderFactory;
import com.larseckart.core.domain.ConversationContext;
import com.larseckart.core.ports.AIProvider;
import com.larseckart.core.ports.input.InputPort;
import com.larseckart.core.ports.output.OutputPort;
import com.larseckart.core.services.ChatService;
import com.larseckart.core.services.ConversationService;

public class CliApplication {

  public static void main(String[] args) {
    System.setProperty("app.mode", "cli");

    InputPort inputPort = new ConsoleInputAdapter();
    OutputPort outputPort = new ConsoleOutputAdapter();

    ConversationContext context = new ConversationContext();
    AIProvider aiProvider = AIProviderFactory.createFromEnvironment();

    ConversationService conversationService = new ConversationService(context, aiProvider);

    ChatService chatService = new ChatService(inputPort, outputPort, conversationService);
    chatService.startChat();
  }
}
