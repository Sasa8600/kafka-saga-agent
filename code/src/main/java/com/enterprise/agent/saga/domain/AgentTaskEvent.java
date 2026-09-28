package com.enterprise.agent.saga.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskEvent implements Serializable {

    private String eventId;
    private String sagaId;
    private String stepId;
    private int stepIndex;
    private String toolName;
    private String inputPayload;
    private String compensationAction;
    private boolean simulateFailure;
    private boolean requireApproval;
    private Instant timestamp;
}
