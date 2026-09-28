package com.enterprise.agent.saga.controller;

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

    @GetMapping("/{sagaId}")
    public ResponseEntity<SagaDetailResponse> getSagaDetails(@PathVariable String sagaId) {
        SagaDetailResponse response = sagaCoordinator.getSagaDetails(sagaId);
        return ResponseEntity.ok(response);
    }
}
