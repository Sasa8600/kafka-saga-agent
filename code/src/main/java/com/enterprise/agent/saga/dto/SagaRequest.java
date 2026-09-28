package com.enterprise.agent.saga.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaRequest {

    @NotBlank(message = "Goal is required")
    private String goal;

    @NotBlank(message = "Initiator is required")
    private String initiator;

    private boolean simulateFailure;
}
