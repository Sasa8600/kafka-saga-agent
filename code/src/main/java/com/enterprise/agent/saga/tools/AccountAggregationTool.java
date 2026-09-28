package com.enterprise.agent.saga.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountAggregationTool implements AgentTool {

    @Override
    public String getName() {
        return "AccountAggregationTool";
    }

    @Override
    public String execute(String inputPayload) {
        log.info("[AccountAggregationTool] Aggregating accounts for payload: {}", inputPayload);
        return "{\"status\":\"SUCCESS\", \"accountsCount\": 4, \"totalBalanceUSD\": 128500.00, \"currency\": \"USD\"}";
    }

    @Override
    public void compensate(String inputPayload, String outputPayload) {
        log.warn("[AccountAggregationTool] Compensating/Releasing account aggregation locks for: {}", inputPayload);
    }
}
