package com.enterprise.agent.saga.controller;

import com.enterprise.agent.saga.dto.ApprovalRequest;
import com.enterprise.agent.saga.dto.SagaDetailResponse;
import com.enterprise.agent.saga.dto.SagaRequest;
import com.enterprise.agent.saga.dto.SagaResponse;
import com.enterprise.agent.saga.orchestrator.SagaCoordinator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sagas")
@RequiredArgsConstructor
public class SagaController {

    private final SagaCoordinator sagaCoordinator;

    @PostMapping
    public ResponseEntity<SagaResponse> submitSaga(@Valid @RequestBody SagaRequest request) {
        SagaResponse response = sagaCoordinator.startSaga(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulate-failure")
    public ResponseEntity<SagaResponse> simulateFailureSaga(@Valid @RequestBody SagaRequest request) {
        request.setSimulateFailure(true);
        SagaResponse response = sagaCoordinator.startSaga(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/high-value-approval")
    public ResponseEntity<SagaResponse> submitHighValueSaga(@Valid @RequestBody SagaRequest request) {
        request.setRequireApproval(true);
        SagaResponse response = sagaCoordinator.startSaga(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sagaId}/approve")
    public ResponseEntity<SagaResponse> approveSaga(
            @PathVariable String sagaId,
            @RequestBody(required = false) ApprovalRequest approval) {
        if (approval == null) {
            approval = ApprovalRequest.builder().approvedBy("SeniorComplianceOfficer").notes("Overridden via UI").build();
        }
        SagaResponse response = sagaCoordinator.approveStep(sagaId, approval);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sagaId}/reject")
    public ResponseEntity<SagaResponse> rejectSaga(
            @PathVariable String sagaId,
            @RequestBody(required = false) ApprovalRequest approval) {
        if (approval == null) {
            approval = ApprovalRequest.builder().approvedBy("SeniorComplianceOfficer").notes("Rejected by compliance operator").build();
        }
        SagaResponse response = sagaCoordinator.rejectStep(sagaId, approval);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sagaId}")
    public ResponseEntity<SagaDetailResponse> getSagaDetails(@PathVariable String sagaId) {
        SagaDetailResponse response = sagaCoordinator.getSagaDetails(sagaId);
        return ResponseEntity.ok(response);
    }
}
