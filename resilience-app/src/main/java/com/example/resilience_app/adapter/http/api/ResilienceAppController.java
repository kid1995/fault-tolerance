package com.example.resilience_app.adapter.http.api;

import com.example.resilience_app.adapter.http.adapter.TroubleMakerAdapter;
import com.example.resilience_app.model.ErrorTestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class ResilienceAppController {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceAppController.class);

    private final TroubleMakerAdapter troubleMakerAdapter;

    public ResilienceAppController(TroubleMakerAdapter troubleMakerAdapter) {
        this.troubleMakerAdapter = troubleMakerAdapter;
    }

    /**
     * Test PROGRAMMATIC retry configuration
     * Uses Feign builder with custom RetryConfig (Database-friendly strategy)
     */
    @PostMapping("/programmatic/retry")
    public ResponseEntity<Map<String, Object>> testProgrammaticRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        if (errorRequest == null) {
            errorRequest = createDefaultErrorRequest();
        }

        logger.info("=== Testing PROGRAMMATIC Retry Strategy ===");
        logger.info("Configuration: Feign Builder + Custom RetryConfig");
        logger.info("Error request: {}", errorRequest);

        try {
            String result = troubleMakerAdapter.simulateErrorWithProgrammaticRetry(errorRequest);
            logger.info("Programmatic retry call successful: {}", result);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "strategy", "PROGRAMMATIC - Feign Builder + Database-friendly RetryConfig",
                    "configuration", "4 attempts, exponential random backoff (200ms-10s)",
                    "result", result,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Programmatic retry call failed after all retries: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "FAILED",
                            "strategy", "PROGRAMMATIC - Feign Builder + Database-friendly RetryConfig",
                            "configuration", "4 attempts, exponential random backoff (200ms-10s)",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Test @QUALIFIER retry configuration
     * Uses @Qualifier annotation with YAML configuration
     */
    @PostMapping("/qualifier/retry")
    public ResponseEntity<Map<String, Object>> testQualifierRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        if (errorRequest == null) {
            errorRequest = createDefaultErrorRequest();
        }

        logger.info("=== Testing @QUALIFIER Retry Strategy ===");
        logger.info("Configuration: @Qualifier + YAML Config");
        logger.info("Error request: {}", errorRequest);

        try {
            String result = troubleMakerAdapter.simulateErrorWithQualifierRetry(errorRequest);
            logger.info("Qualifier retry call successful: {}", result);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "strategy", "@QUALIFIER - YAML Configuration",
                    "configuration", "3 attempts, exponential backoff (1s with 2x multiplier)",
                    "result", result,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Qualifier retry call failed after all retries: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "FAILED",
                            "strategy", "@QUALIFIER - YAML Configuration",
                            "configuration", "3 attempts, exponential backoff (1s with 2x multiplier)",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Test @RETRY ANNOTATION configuration
     * Uses @Retry annotation with YAML configuration
     */
    @PostMapping("/annotation/retry")
    public ResponseEntity<Map<String, Object>> testAnnotationRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        if (errorRequest == null) {
            errorRequest = createDefaultErrorRequest();
        }

        logger.info("=== Testing @RETRY ANNOTATION Strategy ===");
        logger.info("Configuration: @Retry Annotation + YAML Config");
        logger.info("Error request: {}", errorRequest);

        try {
            String result = troubleMakerAdapter.simulateErrorWithAnnotationRetry(errorRequest);
            logger.info("Annotation retry call successful: {}", result);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "strategy", "@RETRY ANNOTATION - YAML Configuration",
                    "configuration", "5 attempts, exponential backoff (500ms with 1.5x multiplier)",
                    "result", result,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Annotation retry call failed after all retries: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "FAILED",
                            "strategy", "@RETRY ANNOTATION - YAML Configuration",
                            "configuration", "5 attempts, exponential backoff (500ms with 1.5x multiplier)",
                            "error", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Test all retry strategies in sequence
     */
    @PostMapping("/all/retry")
    public ResponseEntity<Map<String, Object>> testAllRetryStrategies(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        if (errorRequest == null) {
            errorRequest = createDefaultErrorRequest();
        }

        logger.info("=== Testing ALL Retry Strategies ===");
        logger.info("Error request: {}", errorRequest);

        Map<String, Object> results = Map.of(
                "programmaticRetry", testProgrammaticRetry(errorRequest).getBody(),
                "qualifierRetry", testQualifierRetry(errorRequest).getBody(),
                "annotationRetry", testAnnotationRetry(errorRequest).getBody(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok(results);
    }

    /**
     * Get current retry configurations info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getRetryInfo() {
        logger.info("=== Retry Configuration Info ===");

        Map<String, Object> info = Map.of(
                "strategies", Map.of(
                        "programmatic", Map.of(
                                "approach", "Feign Builder + Custom RetryConfig",
                                "class", "ProgrammaticRetryConfig",
                                "client", "ProgrammaticRetryClient",
                                "strategy", "Database-friendly retry",
                                "maxAttempts", 4,
                                "intervalFunction", "Exponential Random Backoff (200ms-10s)",
                                "endpoint", "POST /api/test/programmatic/retry"
                        ),
                        "qualifier", Map.of(
                                "approach", "@Qualifier + YAML Configuration",
                                "client", "QualifierRetryClient",
                                "yamlKey", "qualifierRetryConfig",
                                "maxAttempts", 3,
                                "waitDuration", "1s with exponential backoff (2x multiplier)",
                                "endpoint", "POST /api/test/qualifier/retry"
                        ),
                        "annotation", Map.of(
                                "approach", "@Retry Annotation + YAML Configuration",
                                "client", "AnnotationRetryService",
                                "method", "@Retry(name = \"annotationRetryConfig\")",
                                "yamlKey", "annotationRetryConfig",
                                "maxAttempts", 5,
                                "waitDuration", "500ms with exponential backoff (1.5x multiplier)",
                                "endpoint", "POST /api/test/annotation/retry"
                        )
                ),
                "allStrategies", "POST /api/test/all/retry",
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok(info);
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