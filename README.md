# Fault Tolerance Simulation with Resilience4j

This project demonstrates fault tolerance between Spring services using Resilience4j. It simulates error scenarios and showcases how Resilience4j's retry mechanism handles them.

## Introduction

Two Spring Boot applications are used in this project:

* **`resilience-app`**: This application integrates Resilience4j to implement retry logic. It acts as a client, making requests to the `trouble-maker` service.
* **`trouble-maker`**: This application simulates a faulty service, responding with errors based on configurations provided by the `resilience-app`.

The `resilience-app` exposes an API that allows defining the `trouble-maker`'s behavior, specifying error codes and their occurrence rates ("error-rate").

## How To Use

1. **Start both applications:** Navigate to the root directory of each application (`resilience-app` and `trouble-maker`) and start them.
   * **IntelliJ:** Click the "Run" button.
   * **Command Line (macOS/Linux):** `./gradlew bootRun`
   * **Command Line (Windows):** `gradlew.bat bootRun`

2. **Simulate requests:** Use the provided Bruno HTTP client collection (`./bruno/relisience-app`) to send requests to the `resilience-app`. **Open the collection directly in your HTTP client instead of importing it.** These pre-configured requests define the behavior of the `trouble-maker`.

## Simulation Scenarios

### 1. Direct HTTP Retry Simulation

**Bruno Requests:**
- **`Programmatic Retry`** - Tests programmatic retry configuration using Feign Builder with custom RetryConfig
- **`Annotation Retry`** - Tests @Retry annotation configuration with YAML settings

The basic simulation process can be broken down into the following steps:

1. **Request Configuration:** The HTTP client sends a request to `resilience-app`, specifying the desired error type and frequency for the `trouble-maker` to simulate.
2. **Request Forwarding:** The `resilience-app` acts as a proxy, forwarding the request to the `trouble-maker` service using OpenFeign.
3. **Fault Injection:** The `trouble-maker` receives the request and, based on the configured parameters, simulates an error. Randomization is used to determine whether a successful response or an error is returned.
4. **Retry Mechanism:** If an error occurs, `resilience-app` utilizes Resilience4j to retry the request to the `trouble-maker`. This continues until a successful response is received or the maximum retry attempts are reached.
5. **Response Handling:** If a successful response is received (either directly or after retries), `resilience-app` returns a success response to the client. If the maximum retry attempts are reached without a successful response, `resilience-app` executes a fallback method defined in the retry configuration.

### 2. Kafka-Based Fault Simulation

**Bruno Request:**
- **`Kafka-Consume-Retry`** - Tests retry mechanisms within Kafka consumer context with configurable message count

This simulation observes the behavior of retry mechanisms within Kafka consumers and identifies potential side effects on message processing.

#### Objective
Investigate what happens when HTTP retries are executed within a Kafka consumer context and whether this causes any adverse effects such as:
- Message processing delays
- Consumer lag accumulation
- Resource exhaustion
- Connection timeouts

#### Setup
Through the `resilience-app` endpoint, you can specify the number of messages to process. The producer is configured to send messages in batches, simulating realistic message flow patterns.

#### Process Flow

1. **HTTP Request with Batch Configuration:** Send an HTTP request to `resilience-app` containing both the error simulation configuration (same as the direct simulation) and the number of messages to process.

2. **Message Production:** Instead of directly calling the `trouble-maker` service, the endpoint uses a Kafka producer to:
   - Package the error simulation configuration into Kafka messages
   - Send the specified number of messages to the Kafka broker
   - Support both sequential and concurrent message sending strategies

3. **Message Consumption:** A Kafka consumer implemented within `resilience-app` processes the messages:
   - Polls messages from the Kafka topics
   - Extracts the error simulation configuration from each message
   - Executes the same HTTP retry logic as the direct simulation

4. **Retry Execution in Consumer Context:** When HTTP requests fail during message processing:
   - Resilience4j retry mechanisms are triggered within the consumer thread
   - The simulation observes the impact on overall message throughput
   - Monitors for consumer lag and processing delays

## Prerequisites

### Kafka Infrastructure
Before running the Kafka-based simulation, ensure Kafka is running:

```bash
# Start Kafka using Docker Compose
docker-compose up -d

# Verify Kafka is running
docker-compose ps
```

The following topics will be created automatically:
- `resilience-lab-topic`

## Request Parameters Reference

All Bruno requests accept the following JSON body structure for error simulation configuration:

```json
{
  "errorCode": "503",
  "errorRate": 0.8,
  "responseDelayMs": 500,
  "retryAfterSeconds": 5,
  "timeoutDelayMs": 5000,
  "enabled": true,
  "description": "Test description"
}
```

### Parameter Descriptions:
- **`errorCode`**: HTTP error code to simulate (503, 500, 502, 504, 429, 408)
- **`errorRate`**: Probability of error occurrence (0.0 = no errors, 1.0 = always error)
- **`responseDelayMs`**: Simulated server response delay in milliseconds
- **`retryAfterSeconds`**: Value for "Retry-After" header in error responses
- **`timeoutDelayMs`**: Timeout delay for 504/408 errors
- **`enabled`**: Enable/disable error simulation
- **`description`**: Human-readable description of the test scenario

### Kafka-Consume-Retry Specific:
The `Kafka-Consume-Retry` request includes an additional path parameter:
- **`numOfMessages`**: Number of messages to send to Kafka topic (configurable in Bruno request path)

## Running Simulations

1. **Start Infrastructure:** `docker-compose up -d`
2. **Start Applications:** Run both `resilience-app` and `trouble-maker`
3. **Execute Tests:** Use Bruno collection requests:
   - For direct HTTP retry testing: `Programmatic Retry` or `Annotation Retry`
   - For Kafka consumer retry testing: `Kafka-Consume-Retry`
