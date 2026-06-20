package com.permission.agent.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.permission.agent.agent.AgentContext;

import java.util.List;
import java.util.Map;

public interface AgentTool {
    String name();
    String description();
    Map<String, Object> parametersSchema();
    boolean requiresConfirmation();
    List<String> requiredPermissions();
    String execute(AgentContext ctx, JsonNode args);
}
