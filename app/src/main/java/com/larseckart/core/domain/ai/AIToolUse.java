package com.larseckart.core.domain.ai;

import com.fasterxml.jackson.databind.JsonNode;

public record AIToolUse(String toolName, JsonNode parameters) {}
