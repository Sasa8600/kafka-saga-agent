package com.enterprise.agent.saga.orchestrator;

import com.enterprise.agent.saga.domain.AgentTaskEvent;
import com.enterprise.agent.saga.domain.SagaInstance;
import com.enterprise.agent.saga.domain.SagaStatus;
import com.enterprise.agent.saga.domain.SagaStep;
import com.enterprise.agent.saga.domain.StepStatus;
import com.enterprise.agent.saga.dto.SagaDetailResponse;
import com.enterprise.agent.saga.dto.SagaRequest;
import com.enterprise.agent.saga.dto.SagaResponse;
import com.enterprise.agent.saga.event.SagaEventProducer;
import com.enterprise.agent.saga.repository.SagaInstanceRepository;
import com.enterprise.agent.saga.repository.SagaStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCoordinator {

    private final SagaInstanceRepository sagaRepository;
    private final SagaStepRepository stepRepository;
    private final PlannerAgent plannerAgent;
    private final SagaEventProducer eventProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public SagaResponse startSaga(SagaRequest request) {
        String sagaId = UUID.randomUUID().toString();
        log.info("[SagaCoordinator] Starting saga {} for goal: {}", sagaId, request.getGoal());

        SagaInstance saga = SagaInstance.builder()
                .id(sagaId)
                .goal(request.getGoal())
                .initiator(request.getInitiator())
                .status(SagaStatus.SUBMITTED)
                .currentStepIndex(0)
                .totalSteps(0)
                .build();

        List<SagaStep> steps = plannerAgent.plan(saga, request.isSimulateFailure());
        saga.setSteps(steps);
        saga.setTotalSteps(steps.size());
        saga.setStatus(SagaStatus.IN_PROGRESS);

        sagaRepository.save(saga);

        redisTemplate.opsForValue().set("saga:state:" + sagaId, SagaStatus.IN_PROGRESS.name(), Duration.ofHours(1));

        if (!steps.isEmpty()) {
            SagaStep firstStep = steps.get(0);
            AgentTaskEvent event = AgentTaskEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .sagaId(sagaId)
                    .stepId(firstStep.getId())
                    .stepIndex(firstStep.getStepIndex())
                    .toolName(firstStep.getToolName())
                    .inputPayload(firstStep.getInputPayload())
                    .compensationAction(firstStep.getCompensationAction())
                    .simulateFailure(request.isSimulateFailure())
                    .timestamp(Instant.now())
                    .build();

            eventProducer.publishTask(event);
        }

        return SagaResponse.builder()
                .sagaId(sagaId)
                .status(SagaStatus.IN_PROGRESS)
                .totalSteps(steps.size())
                .message("Saga accepted and first agent task dispatched to Kafka")
                .createdAt(Instant.now())
                .build();
    }

    public SagaDetailResponse getSagaDetails(String sagaId) {
        SagaInstance saga = sagaRepository.findById(sagaId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + sagaId));

        List<SagaDetailResponse.StepSummary> stepSummaries = saga.getSteps().stream()
                .map(s -> SagaDetailResponse.StepSummary.builder()
                        .stepId(s.getId())
                        .stepIndex(s.getStepIndex())
                        .stepName(s.getStepName())
                        .toolName(s.getToolName())
                        .status(s.getStatus())
                        .inputPayload(s.getInputPayload())
                        .outputPayload(s.getOutputPayload())
                        .compensationAction(s.getCompensationAction())
                        .errorMessage(s.getErrorMessage())
                        .executedAt(s.getExecutedAt())
                        .build())
                .collect(Collectors.toList());

        return SagaDetailResponse.builder()
                .sagaId(saga.getId())
                .goal(saga.getGoal())
                .initiator(saga.getInitiator())
                .status(saga.getStatus())
                .currentStepIndex(saga.getCurrentStepIndex())
                .totalSteps(saga.getTotalSteps())
                .errorMessage(saga.getErrorMessage())
                .steps(stepSummaries)
                .createdAt(saga.getCreatedAt())
                .updatedAt(saga.getUpdatedAt())
                .build();
    }
}
