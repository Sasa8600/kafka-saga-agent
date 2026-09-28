package com.enterprise.agent.saga.repository;

import com.enterprise.agent.saga.domain.SagaStep;
import com.enterprise.agent.saga.domain.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep, String> {
    List<SagaStep> findBySagaInstanceIdOrderByStepIndexAsc(String sagaId);
    List<SagaStep> findBySagaInstanceIdAndStatusOrderByStepIndexDesc(String sagaId, StepStatus status);
    Optional<SagaStep> findBySagaInstanceIdAndStepIndex(String sagaId, int stepIndex);
}
