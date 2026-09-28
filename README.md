# Kafka-Saga-Agent

> **Durable, Event-Driven Long-Running Multi-Agent Orchestrator**  
> *Built with Spring Boot 3, Apache Kafka, Redis, PostgreSQL, and Spring AI.*

---

## 🌟 Why This Project?
Most AI agent frameworks (LangChain, CrewAI) execute multi-step reasoning **synchronously in-memory**. If a network timeout or node crash occurs mid-execution:
1. State is permanently lost.
2. Non-idempotent tool calls (e.g. fund reservations or ledger mutations) cannot be recovered.
3. No compensation or rollback exists for downstream failures.

**Kafka-Saga-Agent** brings the enterprise **Distributed Saga Pattern** to Agentic AI:
- **Event-Driven Execution:** Every task, state change, and tool result is an immutable Kafka event.
- **Fault-Tolerant Checkpoints:** Short-term state & locks in Redis; durable audit trail in PostgreSQL.
- **Automatic Self-Healing & Compensation:** If an agent encounters a business/tool failure, it executes reverse compensating actions in LIFO order.

---

## 🚀 Quickstart (1-Command Docker Setup)

```bash
# 1. Start Kafka (KRaft), Redis, and PostgreSQL
cd code/docker
docker compose up -d

# 2. Run the Spring Boot App in IntelliJ
# Main class: com.enterprise.agent.saga.SagaAgentApplication
```

### Test via cURL

**1. Normal Saga (All Steps Succeed):**
```bash
curl -X POST http://localhost:8080/api/v1/sagas \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Perform multi-account financial audit and transaction validation for client #98213",
    "initiator": "FintechOps"
  }'
```

**2. Failure & Rollback Test (Triggers Automatic Compensating Actions):**
```bash
curl -X POST http://localhost:8080/api/v1/sagas/simulate-failure \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Execute high-risk ledger transfer with simulated AML failure",
    "initiator": "QA-Tester"
  }'
```

**3. Inspect Saga Execution & Audit Timeline:**
```bash
curl -X GET http://localhost:8080/api/v1/sagas/{sagaId}
```

---

## 🖥️ Live Real-Time Dashboard UI (100x Impact)
Once the Spring Boot application is running, open your web browser:

👉 **`http://localhost:8080`**

### Features:
- **Interactive Control Center:** Trigger normal sagas or test simulated AML failures with 1 click.
- **Visual DAG Pipeline:** Watch steps transition live from `PENDING` ➔ `RUNNING` ➔ `COMPLETED` (or `COMPENSATING` ➔ `COMPENSATED` on failure).
- **Streaming Event Terminal:** Real-time log showing Kafka message dispatches, Redis locks, and execution durations.
- **Standalone Web App:** Source code also mirrored in `/web-app/index.html`.

