package com.enterprise.agent.saga.domain;

public enum StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
