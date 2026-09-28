package com.enterprise.agent.saga.event;

import com.enterprise.agent.saga.domain.AgentTaskEvent;
import com.enterprise.agent.saga.domain.SagaInstance;
import com.enterprise.agent.saga.domain.SagaStatus;
import com.enterprise.agent.saga.domain.SagaStep;
import com.enterprise.agent.saga.domain.StepStatus;
import com.enterprise.agent.saga.orchestrator.CompensationEngine;
import com.enterprise.agent.saga.repository.SagaInstanceRepository;
import com.enterprise.agent.saga.repository.SagaStepRepository;
import com.enterprise.agent.saga.tools.AgentTool;
import com.enterprise.agent.saga.tools.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaWorkerConsumer {

    private final ToolRegistry toolRegistry;
    private final SagaInstanceRepository sagaRepository;
    private final SagaStepRepository stepRepository;
    private final SagaEventProducer eventProducer;
    private final CompensationEngine compensationEngine;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "${saga.topics.tasks:saga.agent.tasks}", groupId = "saga-agent-group")
    @Transactional
    public void consumeTask(AgentTaskEvent event) {
        String lockKey = "lock:saga:" + event.getSagaId() + ":step:" + event.getStepIndex();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(acquired)) {
            log.warn("[KafkaWorker] Duplicate task detected or lock already held for: {}", lockKey);
            return;
        }

        try {
            log.info("[KafkaWorker] Executing step {} for sagaId: {}, tool: {}",
                    event.getStepIndex(), event.getSagaId(), event.getToolName());

            SagaStep step = stepRepository.findById(event.getStepId()).orElseThrow();
            step.setStatus(StepStatus.RUNNING);
            stepRepository.save(step);

            AgentTool tool = toolRegistry.getTool(event.getToolName());
            String output = tool.execute(event.getInputPayload());

            step.setOutputPayload(output);

            if (output != null && output.contains("SUSPENDED_FOR_APPROVAL")) {
                log.warn("[KafkaWorker] Step {} requires Human-in-the-Loop approval! Suspending saga {}",
                        event.getStepIndex(), event.getSagaId());
                step.setStatus(StepStatus.WAITING_FOR_APPROVAL);
                stepRepository.save(step);

                SagaInstance saga = sagaRepository.findById(event.getSagaId()).orElseThrow();
                saga.setStatus(SagaStatus.WAITING_FOR_APPROVAL);
                sagaRepository.save(saga);

                redisTemplate.opsForValue().set("saga:state:" + event.getSagaId(), SagaStatus.WAITING_FOR_APPROVAL.name());
                return;
            }

            step.setStatus(StepStatus.COMPLETED);
            step.setExecutedAt(Instant.now());
            stepRepository.save(step);

            advanceSaga(event);

        } catch (Exception ex) {
            log.error("[KafkaWorker] Step execution failed for saga {}: {}", event.getSagaId(), ex.getMessage());
            stepRepository.findById(event.getStepId()).ifPresent(s -> {
                s.setStatus(StepStatus.FAILED);
                s.setErrorMessage(ex.getMessage());
                stepRepository.save(s);
            });
            compensationEngine.initiateCompensation(event.getSagaId(), ex.getMessage());
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public void advanceSaga(AgentTaskEvent completedEvent) {
        SagaInstance saga = sagaRepository.findById(completedEvent.getSagaId()).orElseThrow();
        int nextIndex = completedEvent.getStepIndex() + 1;
        saga.setCurrentStepIndex(nextIndex);

        Optional<SagaStep> nextStepOpt = stepRepository.findBySagaInstanceIdAndStepIndex(saga.getId(), nextIndex);

        if (nextStepOpt.isPresent()) {
            SagaStep nextStep = nextStepOpt.get();
            log.info("[KafkaWorker] Scheduling next step {} ({})", nextIndex, nextStep.getStepName());

            AgentTaskEvent nextEvent = AgentTaskEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .sagaId(saga.getId())
                    .stepId(nextStep.getId())
                    .stepIndex(nextIndex)
                    .toolName(nextStep.getToolName())
                    .inputPayload(nextStep.getInputPayload())
                    .compensationAction(nextStep.getCompensationAction())
                    .timestamp(Instant.now())
                    .build();

            eventProducer.publishTask(nextEvent);
        } else {
            log.info("[KafkaWorker] All {} steps completed! Saga {} is COMPLETED.", saga.getTotalSteps(), saga.getId());
            saga.setStatus(SagaStatus.COMPLETED);
            redisTemplate.opsForValue().set("saga:state:" + saga.getId(), SagaStatus.COMPLETED.name());
        }
        sagaRepository.save(saga);
    }
}
