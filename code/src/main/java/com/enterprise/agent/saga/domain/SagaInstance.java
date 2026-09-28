package com.enterprise.agent.saga.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saga_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaInstance {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goal;

    @Column(nullable = false, length = 128)
    private String initiator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SagaStatus status;

    @Column(name = "current_step_index")
    private int currentStepIndex;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @OneToMany(mappedBy = "sagaInstance", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("stepIndex ASC")
    @Builder.Default
    private List<SagaStep> steps = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
