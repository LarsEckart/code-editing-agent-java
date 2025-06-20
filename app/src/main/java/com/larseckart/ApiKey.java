package com.larseckart;

public class ApiKey {

  private final String value;

  private ApiKey(String value) {
    this.value = value;
  }

  public static ApiKey fromEnvironment(String environmentVariableName) {
    String value = System.getenv(environmentVariableName);
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException(
          "API key cannot be null or empty. Check environment variable: "
              + environmentVariableName);
    }
    return new ApiKey(value);
  }

  public static ApiKey forTesting(String value) {
    return new ApiKey(value);
  }

  public String getValue() {
    return value;
  }
}
