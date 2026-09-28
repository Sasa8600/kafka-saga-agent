package com.enterprise.agent.saga.dto;

import com.enterprise.agent.saga.domain.SagaStatus;
import com.enterprise.agent.saga.domain.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaDetailResponse {

    private String sagaId;
    private String goal;
    private String initiator;
    private SagaStatus status;
    private int currentStepIndex;
    private int totalSteps;
    private String errorMessage;
    private List<StepSummary> steps;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepSummary {
        private String stepId;
        private int stepIndex;
        private String stepName;
        private String toolName;
        private StepStatus status;
        private String inputPayload;
        private String outputPayload;
        private String compensationAction;
        private String errorMessage;
        private Instant executedAt;
    }
}
