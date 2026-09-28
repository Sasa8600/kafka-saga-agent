package com.enterprise.agent.saga.repository;

import com.enterprise.agent.saga.domain.SagaInstance;
import com.enterprise.agent.saga.domain.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, String> {
    List<SagaInstance> findByStatus(SagaStatus status);
}
