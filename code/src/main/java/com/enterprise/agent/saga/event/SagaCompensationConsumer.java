package com.enterprise.agent.saga.event;

import com.enterprise.agent.saga.domain.AgentTaskEvent;
import com.enterprise.agent.saga.domain.SagaInstance;
import com.enterprise.agent.saga.domain.SagaStatus;
import com.enterprise.agent.saga.domain.SagaStep;
import com.enterprise.agent.saga.domain.StepStatus;
import com.enterprise.agent.saga.repository.SagaInstanceRepository;
import com.enterprise.agent.saga.repository.SagaStepRepository;
import com.enterprise.agent.saga.tools.AgentTool;
import com.enterprise.agent.saga.tools.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaCompensationConsumer {

    private final ToolRegistry toolRegistry;
    private final SagaInstanceRepository sagaRepository;
    private final SagaStepRepository stepRepository;

    @KafkaListener(topics = "${saga.topics.compensate:saga.agent.compensate}", groupId = "saga-agent-comp-group")
    @Transactional
    public void consumeCompensation(AgentTaskEvent event) {
        log.warn("[CompensationConsumer] Executing rollback for step {} on saga: {}",
                event.getStepIndex(), event.getSagaId());

        try {
            SagaStep step = stepRepository.findById(event.getStepId()).orElseThrow();
            step.setStatus(StepStatus.COMPENSATING);

            AgentTool tool = toolRegistry.getTool(event.getToolName());
            tool.compensate(event.getInputPayload(), step.getOutputPayload());

            step.setStatus(StepStatus.COMPENSATED);
            stepRepository.save(step);

            checkSagaCompensated(event.getSagaId());

        } catch (Exception ex) {
            log.error("[CompensationConsumer] Failed to compensate step {}: {}", event.getStepIndex(), ex.getMessage());
        }
    }

    private void checkSagaCompensated(String sagaId) {
        SagaInstance saga = sagaRepository.findById(sagaId).orElseThrow();
        boolean hasPendingComp = stepRepository.findBySagaInstanceIdOrderByStepIndexAsc(sagaId).stream()
                .anyMatch(s -> s.getStatus() == StepStatus.COMPENSATING || s.getStatus() == StepStatus.RUNNING);

        if (!hasPendingComp) {
            log.info("[CompensationConsumer] All rollbacks finalized. Saga {} marked as COMPENSATED.", sagaId);
            saga.setStatus(SagaStatus.COMPENSATED);
            sagaRepository.save(saga);
        }
    }
}
