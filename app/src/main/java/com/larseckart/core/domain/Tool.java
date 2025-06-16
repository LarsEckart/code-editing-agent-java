package com.larseckart.core.domain;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a tool that can be used by the AI agent to perform specific tasks.
 * Tools encapsulate functionality with well-defined parameters and execution semantics.
 */
public interface Tool {

  /**
   * Returns the name of this tool.
   * The name should be unique and descriptive, suitable for identification purposes.
   *
   * @return the tool name, never null or empty
   */
  String getName();

  /**
   * Returns a human-readable description of what this tool does.
   * This description should clearly explain the tool's purpose and functionality.
   *
   * @return the tool description, never null or empty
   */
  String getDescription();

  /**
   * Returns the JSON schema that defines the expected parameters for this tool.
   * The schema should follow JSON Schema specification and validate the parameters
   * that will be passed to the execute method.
   *
   * @return the parameter schema as a JSON string, never null or empty
   */
  String getParameterSchema();

  /**
   * Executes this tool with the provided parameters.
   * The parameters must conform to the schema returned by getParameterSchema().
   *
   * @param parameters the execution parameters as a JsonNode, must not be null
   * @return the result of the tool execution as a string, never null
   * @throws IllegalArgumentException if parameters are null or invalid
   */
  String execute(JsonNode parameters);

  /**
   * Validates the provided parameters against this tool's schema.
   * This method should be called before execute() to ensure parameter validity.
   *
   * @param parameters the parameters to validate, must not be null
   * @throws IllegalArgumentException if parameters are null or do not conform to the schema
   */
  void validate(JsonNode parameters);
}