package com.enterprise.agent.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Kafka-Saga-Agent: Event-Driven Resilient Multi-Agent Orchestrator
 */
@SpringBootApplication
@EnableAsync
public class SagaAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SagaAgentApplication.class, args);
    }
}
