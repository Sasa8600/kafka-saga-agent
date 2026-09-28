package com.enterprise.agent.saga.domain;

public enum SagaStatus {
    SUBMITTED,
    IN_PROGRESS,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
