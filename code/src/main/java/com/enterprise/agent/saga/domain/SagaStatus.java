package com.enterprise.agent.saga.domain;

public enum SagaStatus {
    SUBMITTED,
    IN_PROGRESS,
    WAITING_FOR_APPROVAL,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
