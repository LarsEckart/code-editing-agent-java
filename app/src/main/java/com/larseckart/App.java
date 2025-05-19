package com.larseckart;

import com.larseckart.tool.ToolDefinition;
import com.larseckart.tools.EditFileTool;
import com.larseckart.tools.ListFilesTool;
import com.larseckart.tools.ReadFileTool;

import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

public class App {

  public static void main(String[] args) {
    // Setup input scanner
    Scanner scanner = new Scanner(System.in);
    Supplier<String> supplier = scanner::nextLine;
    
    // Create tools
    List<ToolDefinition> tools = List.of(
        ReadFileTool.create(),
        ListFilesTool.create(),
        EditFileTool.create()
    );
    
    // Create context and client
    Context context = new Context();
    Client client = new Client(context, null, tools);
    
    // Create agent and start it
    new Agent(supplier, client, tools).start();
  }
}
