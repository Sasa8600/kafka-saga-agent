package com.enterprise.agent.saga.event;

import com.enterprise.agent.saga.domain.AgentTaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${saga.topics.tasks:saga.agent.tasks}")
    private String tasksTopic;

    @Value("${saga.topics.compensate:saga.agent.compensate}")
    private String compensateTopic;

    public void publishTask(AgentTaskEvent event) {
        log.info("[KafkaProducer] Publishing task event to {}: sagaId={}, stepIndex={}, tool={}",
                tasksTopic, event.getSagaId(), event.getStepIndex(), event.getToolName());
        kafkaTemplate.send(tasksTopic, event.getSagaId(), event);
    }

    public void publishCompensation(AgentTaskEvent event) {
        log.warn("[KafkaProducer] Publishing compensation event to {}: sagaId={}, stepIndex={}",
                compensateTopic, event.getSagaId(), event.getStepIndex());
        kafkaTemplate.send(compensateTopic, event.getSagaId(), event);
    }
}
