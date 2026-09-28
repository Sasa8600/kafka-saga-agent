package com.enterprise.agent.saga.dto;

import com.enterprise.agent.saga.domain.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaResponse {

    private String sagaId;
    private SagaStatus status;
    private int totalSteps;
    private String message;
    private Instant createdAt;
}
