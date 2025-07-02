# Fault Tolerance Simulation with Resilience4j

This project demonstrates fault tolerance between Spring services using Resilience4j.  It simulates error scenarios and showcases how Resilience4j's retry mechanism handles them.

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

2. **Simulate requests:** Use the provided HTTP client collection (`./http-client/relisience-app` relative to the project root) to send requests to the `resilience-app`.  **Open the collection directly in your HTTP client instead of importing it.**  These pre-configured requests define the behavior of the `trouble-maker`.

## Simulation Steps

The simulation process can be broken down into the following steps:

1. **Request Configuration:** The HTTP client sends a request to `resilience-app`, specifying the desired error type and frequency for the `trouble-maker` to simulate.
2. **Request Forwarding:** The `resilience-app` acts as a proxy, forwarding the request to the `trouble-maker` service using OpenFeign.
3. **Fault Injection:** The `trouble-maker` receives the request and, based on the configured parameters, simulates an error.  Randomization is used to determine whether a successful response or an error is returned.
4. **Retry Mechanism:** If an error occurs, `resilience-app` utilizes Resilience4j to retry the request to the `trouble-maker`.  This continues until a successful response is received or the maximum retry attempts are reached.
5. **Response Handling:** If a successful response is received (either directly or after retries), `resilience-app` returns a success response to the client.  If the maximum retry attempts are reached without a successful response, `resilience-app` executes a fallback method defined in the retry configuration.