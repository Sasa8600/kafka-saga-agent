package com.enterprise.agent.saga.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${saga.topics.tasks:saga.agent.tasks}")
    private String tasksTopic;

    @Value("${saga.topics.compensate:saga.agent.compensate}")
    private String compensateTopic;

    @Value("${saga.topics.events:saga.agent.events}")
    private String eventsTopic;

    @Bean
    public NewTopic tasksTopic() {
        return TopicBuilder.name(tasksTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic compensateTopic() {
        return TopicBuilder.name(compensateTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder.name(eventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
