package com.larseckart.core.domain.ai;

import java.util.Map;

public record AITool(String name, String description, Map<String, Object> inputSchema) {}
