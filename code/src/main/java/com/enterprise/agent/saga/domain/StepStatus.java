package com.enterprise.agent.saga.domain;

public enum StepStatus {
    PENDING,
    RUNNING,
    WAITING_FOR_APPROVAL,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
