package com.enterprise.agent.saga.orchestrator;

import com.enterprise.agent.saga.domain.AgentTaskEvent;
import com.enterprise.agent.saga.domain.SagaInstance;
import com.enterprise.agent.saga.domain.SagaStatus;
import com.enterprise.agent.saga.domain.SagaStep;
import com.enterprise.agent.saga.domain.StepStatus;
import com.enterprise.agent.saga.event.SagaEventProducer;
import com.enterprise.agent.saga.repository.SagaInstanceRepository;
import com.enterprise.agent.saga.repository.SagaStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationEngine {

    private final SagaInstanceRepository sagaRepository;
    private final SagaStepRepository stepRepository;
    private final SagaEventProducer eventProducer;

    @Transactional
    public void initiateCompensation(String sagaId, String failureReason) {
        log.warn("[CompensationEngine] Initiating reverse rollback for saga: {}, reason: {}", sagaId, failureReason);

        SagaInstance saga = sagaRepository.findById(sagaId).orElseThrow();
        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setErrorMessage(failureReason);
        sagaRepository.save(saga);

        List<SagaStep> completedSteps = stepRepository.findBySagaInstanceIdAndStatusOrderByStepIndexDesc(
                sagaId, StepStatus.COMPLETED);

        if (completedSteps.isEmpty()) {
            log.info("[CompensationEngine] No completed steps to compensate. Marking saga as FAILED.");
            saga.setStatus(SagaStatus.FAILED);
            sagaRepository.save(saga);
            return;
        }

        for (SagaStep step : completedSteps) {
            AgentTaskEvent compEvent = AgentTaskEvent.builder()
                    .eventId(step.getId())
                    .sagaId(sagaId)
                    .stepId(step.getId())
                    .stepIndex(step.getStepIndex())
                    .toolName(step.getToolName())
                    .inputPayload(step.getInputPayload())
                    .compensationAction(step.getCompensationAction())
                    .timestamp(Instant.now())
                    .build();

            eventProducer.publishCompensation(compEvent);
        }
    }
}
