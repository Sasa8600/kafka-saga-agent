package com.enterprise.agent.saga.orchestrator;

import com.enterprise.agent.saga.domain.SagaInstance;
import com.enterprise.agent.saga.domain.SagaStep;
import com.enterprise.agent.saga.domain.StepStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class PlannerAgent {

    public List<SagaStep> plan(SagaInstance saga, boolean simulateFailure, boolean requireApproval) {
        log.info("[PlannerAgent] Planning saga steps for goal: {} (failure={}, hitl={})",
                saga.getGoal(), simulateFailure, requireApproval);
        List<SagaStep> steps = new ArrayList<>();

        steps.add(SagaStep.builder()
                .id(UUID.randomUUID().toString())
                .sagaInstance(saga)
                .stepIndex(0)
                .stepName("Account Data Aggregation")
                .toolName("AccountAggregationTool")
                .inputPayload("{\"clientId\": \"CLI-98213\", \"source\": \"CORE_BANKING\"}")
                .compensationAction("ReleaseAccountLocks")
                .status(StepStatus.PENDING)
                .build());

        String fraudInput;
        if (simulateFailure) {
            fraudInput = "{\"clientId\": \"CLI-98213\", \"riskCheck\": \"AML_PEP\", \"flag\": \"SIMULATE_FAILURE\"}";
        } else if (requireApproval) {
            fraudInput = "{\"clientId\": \"CLI-98213\", \"riskCheck\": \"AML_PEP\", \"flag\": \"REQUIRE_APPROVAL\"}";
        } else {
            fraudInput = "{\"clientId\": \"CLI-98213\", \"riskCheck\": \"AML_PEP\"}";
        }

        steps.add(SagaStep.builder()
                .id(UUID.randomUUID().toString())
                .sagaInstance(saga)
                .stepIndex(1)
                .stepName("AML & Fraud Risk Evaluation")
                .toolName("FraudRiskAuditTool")
                .inputPayload(fraudInput)
                .compensationAction("RollbackRiskHold")
                .status(StepStatus.PENDING)
                .build());

        steps.add(SagaStep.builder()
                .id(UUID.randomUUID().toString())
                .sagaInstance(saga)
                .stepIndex(2)
                .stepName("Ledger Balance Reconciliation")
                .toolName("LedgerReconciliationTool")
                .inputPayload("{\"clientId\": \"CLI-98213\", \"action\": \"POST_AUDIT_BALANCES\"}")
                .compensationAction("ReverseLedgerEntry")
                .status(StepStatus.PENDING)
                .build());

        return steps;
    }
}
