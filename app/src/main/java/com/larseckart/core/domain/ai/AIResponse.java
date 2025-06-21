package com.larseckart.core.domain.ai;

import java.util.List;

public record AIResponse(String textContent, List<AIToolUse> toolUses, boolean hasToolUse) {}
