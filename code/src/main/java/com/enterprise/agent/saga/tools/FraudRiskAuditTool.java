package com.enterprise.agent.saga.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FraudRiskAuditTool implements AgentTool {

    @Override
    public String getName() {
        return "FraudRiskAuditTool";
    }

    @Override
    public String execute(String inputPayload) throws Exception {
        log.info("[FraudRiskAuditTool] Evaluating AML and transaction risk: {}", inputPayload);
        if (inputPayload != null && inputPayload.contains("SIMULATE_FAILURE")) {
            log.error("[FraudRiskAuditTool] Triggering simulated risk failure!");
            throw new IllegalStateException("CRITICAL_FRAUD_TRIGGER: Simulated AML sanction match detected");
        }
        return "{\"status\":\"PASSED\", \"riskScore\": 12, \"verdict\": \"LOW_RISK_APPROVED\"}";
    }

    @Override
    public void compensate(String inputPayload, String outputPayload) {
        log.warn("[FraudRiskAuditTool] Rolling back fraud risk hold on account: {}", inputPayload);
    }
}
