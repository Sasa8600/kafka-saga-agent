package com.enterprise.agent.saga.tools;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();

    public ToolRegistry(List<AgentTool> agentTools) {
        for (AgentTool tool : agentTools) {
            tools.put(tool.getName(), tool);
        }
    }

    public AgentTool getTool(String name) {
        AgentTool tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        return tool;
    }
}
