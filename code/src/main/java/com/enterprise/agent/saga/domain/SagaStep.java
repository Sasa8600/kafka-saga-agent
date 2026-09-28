package com.enterprise.agent.saga.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "saga_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStep {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_id", nullable = false)
    @JsonIgnore
    private SagaInstance sagaInstance;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "step_name", nullable = false, length = 128)
    private String stepName;

    @Column(name = "tool_name", nullable = false, length = 128)
    private String toolName;

    @Column(name = "input_payload", columnDefinition = "TEXT")
    private String inputPayload;

    @Column(name = "output_payload", columnDefinition = "TEXT")
    private String outputPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StepStatus status;

    @Column(name = "compensation_action", length = 128)
    private String compensationAction;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "executed_at")
    private Instant executedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
