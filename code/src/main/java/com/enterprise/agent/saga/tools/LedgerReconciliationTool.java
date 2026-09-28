package com.enterprise.agent.saga.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LedgerReconciliationTool implements AgentTool {

    @Override
    public String getName() {
        return "LedgerReconciliationTool";
    }

    @Override
    public String execute(String inputPayload) {
        log.info("[LedgerReconciliationTool] Reconciling ledger state: {}", inputPayload);
        return "{\"status\":\"RECONCILED\", \"discrepancies\": 0, \"ledgerId\": \"LEDG-88219\"}";
    }

    @Override
    public void compensate(String inputPayload, String outputPayload) {
        log.warn("[LedgerReconciliationTool] Reversing ledger entry: {}", outputPayload);
    }
}
