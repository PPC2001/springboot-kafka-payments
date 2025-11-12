# Spring Boot Kafka Payments

This project demonstrates a **Spring Boot Kafka Producer and Consumer** setup for handling **Payments, Refunds, and Notifications**.

---

## Features

- **Kafka Producer** for:
  - Payment transactions
  - Refund transactions
  - Notification events (EMAIL, SMS, PUSH)
  - Payment logs
  - Payment failure handling (Dead Letter Topic)

- **Kafka Consumer** for:
  - Processing payments with idempotency
  - Processing refunds with idempotency
  - Handling notifications (EMAIL, SMS, PUSH)
  - Logging and auditing events
  - Error handling for failed events

- **Transactional Kafka messaging** ensures atomic event publishing.

- Implemented using **Java 21**, **Spring Boot 3.x**, and **Spring Kafka 3.x**.

---

## Getting Started

### Prerequisites

- Java 21
- Maven 4.x
- Docker (for Kafka)
- Spring Boot 3.x
- Apache Kafka

### Run Kafka using Docker

```bash
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.8
docker run -d --name kafka -p 9092:9092 --env KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 --env KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 bitnami/kafka:latest
