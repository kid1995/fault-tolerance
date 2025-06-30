package com.example.service_a.adapter.http.api;

import com.example.service_a.adapter.http.adapter.TroubleMakerAdapter;
import com.example.service_a.model.ErrorTestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class FaultToleranceTestController {

    private static final Logger logger = LoggerFactory.getLogger(FaultToleranceTestController.class);

    private final TroubleMakerAdapter troubleMakerAdapter;

    public FaultToleranceTestController(TroubleMakerAdapter troubleMakerAdapter) {
        this.troubleMakerAdapter = troubleMakerAdapter;
    }

    /**
     * Test Service B with programmatic retry configuration
     * Uses Resilience4j Feign decorator with custom RetryConfig
     */
    @PostMapping("/service-b/retry")
    public ResponseEntity<Map<String, Object>> testServiceBRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        if (errorRequest == null) {
            errorRequest = createDefaultErrorRequest();
        }

        logger.info("=== Testing Service B Retry Strategy ===");
        logger.info("Error request: {}", errorRequest);

        try {
            String result = troubleMakerAdapter.getResourceFromServiceB(errorRequest);
            logger.info("Service B call successful: {}", result);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "strategy", "Service B - Programmatic Retry",
                    "result", result,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Service B call failed after all retries: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "FAILED",
                            "strategy", "Service B - Programmatic Retry",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Test Service C with @Qualifier annotation retry configuration
     * Uses application.yml configuration with serviceCRetry qualifier
     */
    @PostMapping("/service-c/retry")
    public ResponseEntity<Map<String, Object>> testServiceCRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        if (errorRequest == null) {
            errorRequest = createDefaultErrorRequest();
        }

        logger.info("=== Testing Service C Retry Strategy ===");
        logger.info("Error request: {}", errorRequest);

        try {
            String result = troubleMakerAdapter.getResourceFromServiceC(errorRequest);
            logger.info("Service C call successful: {}", result);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "strategy", "Service C - @Qualifier Retry (YAML Config)",
                    "result", result,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Service C call failed after all retries: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "FAILED",
                            "strategy", "Service C - @Qualifier Retry (YAML Config)",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Test TroubleMaker with @Retry annotation
     * Uses application.yml configuration with troubleMakerRetry
     */
    @PostMapping("/trouble-maker/retry")
    public ResponseEntity<Map<String, Object>> testTroubleMakerRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        if (errorRequest == null) {
            errorRequest = createDefaultErrorRequest();
        }

        logger.info("=== Testing TroubleMaker Retry Strategy ===");
        logger.info("Error request: {}", errorRequest);

        try {
            String result = troubleMakerAdapter.callTroubleMakerWithError(errorRequest);
            logger.info("TroubleMaker call successful: {}", result);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "strategy", "TroubleMaker - @Retry Annotation (YAML Config)",
                    "result", result,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("TroubleMaker call failed after all retries: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "FAILED",
                            "strategy", "TroubleMaker - @Retry Annotation (YAML Config)",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Create default error request for testing
     */
    private ErrorTestRequest createDefaultErrorRequest() {
        ErrorTestRequest defaultRequest = new ErrorTestRequest();
        defaultRequest.setErrorCode("503");
        defaultRequest.setErrorRate(0.8); // 80% error rate
        defaultRequest.setResponseDelayMs(1000);
        defaultRequest.setRetryAfterSeconds(5);
        defaultRequest.setEnabled(true);
        defaultRequest.setDescription("Default test error configuration");

        logger.info("Using default error request: {}", defaultRequest);
        return defaultRequest;
    }
}