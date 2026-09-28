package com.enterprise.agent.saga.tools;

public interface AgentTool {
    String getName();
    String execute(String inputPayload) throws Exception;
    void compensate(String inputPayload, String outputPayload) throws Exception;
}
