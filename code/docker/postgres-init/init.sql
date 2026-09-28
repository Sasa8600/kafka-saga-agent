-- Initialize schema for Kafka-Saga-Agent

CREATE TABLE IF NOT EXISTS saga_instances (
    id VARCHAR(64) PRIMARY KEY,
    goal TEXT NOT NULL,
    initiator VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_step_index INT DEFAULT 0,
    total_steps INT NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS saga_steps (
    id VARCHAR(64) PRIMARY KEY,
    saga_id VARCHAR(64) NOT NULL REFERENCES saga_instances(id) ON DELETE CASCADE,
    step_index INT NOT NULL,
    step_name VARCHAR(128) NOT NULL,
    tool_name VARCHAR(128) NOT NULL,
    input_payload TEXT,
    output_payload TEXT,
    status VARCHAR(32) NOT NULL,
    compensation_action VARCHAR(128),
    error_message TEXT,
    executed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_saga_steps_saga_id ON saga_steps(saga_id);
CREATE INDEX IF NOT EXISTS idx_saga_instances_status ON saga_instances(status);
