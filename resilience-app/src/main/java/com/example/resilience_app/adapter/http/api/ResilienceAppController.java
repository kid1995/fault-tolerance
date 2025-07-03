package com.example.resilience_app.adapter.http.api;

import com.example.resilience_app.adapter.http.adapter.TroubleMakerAdapter;
import com.example.resilience_app.adapter.message.MessageProducer;
import com.example.resilience_app.config.service.ProgrammaticRetryConfig;
import com.example.resilience_app.model.ErrorTestRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class ResilienceAppController {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceAppController.class);

    private final TroubleMakerAdapter troubleMakerAdapter;
    private final ProgrammaticRetryConfig programmaticRetryConfig;
    private final ObjectMapper objectMapper;
    private final MessageProducer messageProducer;

    @Value("${resilience4j.retry.instances.annotationRetryConfig.max-attempts}")
    private int annotationMaxAttempts;

    @Value("${resilience4j.retry.instances.annotationRetryConfig.wait-duration}")
    private String annotationWaitDuration;

    @Value("${resilience4j.retry.instances.annotationRetryConfig.exponential-backoff-multiplier}")
    private double annotationMultiplier;

    @Value("${spring.stream.bindings.resilience-lab-in-0.destination}")
    private String RESILIENCE_LAB_BINDING;

    public ResilienceAppController(TroubleMakerAdapter troubleMakerAdapter,
                                   ProgrammaticRetryConfig programmaticRetryConfig,
                                   ObjectMapper objectMapper, MessageProducer messageProducer) {
        this.troubleMakerAdapter = troubleMakerAdapter;
        this.programmaticRetryConfig = programmaticRetryConfig;
        this.objectMapper = objectMapper;
        this.messageProducer = messageProducer;
    }

    /**
     * Test PROGRAMMATIC retry configuration
     * Uses Feign builder with custom RetryConfig (Database-friendly strategy)
     */
    @PostMapping("/programmatic/retry")
    public ResponseEntity<Map<String, Object>> testProgrammaticRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        ErrorTestRequest verifiedErrorTestRequest = checkAndLogErrorTestRequest(errorRequest, "PROGRAMMATIC", "Feign Builder + Custom RetryConfig");
        String strategyDescription = buildProgrammaticStrategyDescription();
        String configurationDescription = buildProgrammaticConfigurationDescription();
        String result = troubleMakerAdapter.simulateErrorWithProgrammaticRetry(verifiedErrorTestRequest);
        logger.info("Programmatic retry call successful: {}", result);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "strategy", strategyDescription,
                "configuration", configurationDescription,
                "result", formatResult(result),
                "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * Test PROGRAMMATIC retry configuration
     * Uses Feign builder with custom RetryConfig (Database-friendly strategy)
     */
    @PostMapping("/programmatic/async/retry")
    public ResponseEntity<Map<String, Object>> testProgrammaticRetryAsync(
            @RequestParam (value = "numOfMsg", required = false, defaultValue = "1") String numOfMsg,
            @RequestBody(required = false) ErrorTestRequest errorRequest) {
        ErrorTestRequest verifiedErrorTestRequest = checkAndLogErrorTestRequest(errorRequest,
                "PROGRAMMATIC-ASYNC",
                "Kafka + Feign Builder + Custom RetryConfig");

        String strategyDescription = buildProgrammaticStrategyDescription();
        String configurationDescription = buildProgrammaticConfigurationDescription();
        messageProducer.sendMessage(
                RESILIENCE_LAB_BINDING,
                verifiedErrorTestRequest);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "strategy", strategyDescription,
                "configuration", configurationDescription,
                "result", "Message sent to programmatic retry binding",
                "timestamp", LocalDateTime.now()
        ));
    }




    /**
     * Test @RETRY ANNOTATION configuration
     * Uses @Retry annotation with YAML configuration
     */
    @PostMapping("/annotation/retry")
    public ResponseEntity<Map<String, Object>> testAnnotationRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        ErrorTestRequest verifiedErrorTestRequest = checkAndLogErrorTestRequest(errorRequest, "ANNOTATION", "@RETRY ANNOTATION + YAML Config");
        String strategyDescription = "@RETRY ANNOTATION - YAML Configuration";
        String configurationDescription = buildAnnotationConfigurationDescription();

        String result = troubleMakerAdapter.simulateErrorWithAnnotationRetry(verifiedErrorTestRequest);
        logger.info("Annotation retry call successful: {}", result);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "strategy", strategyDescription,
                "configuration", configurationDescription,
                "result", formatResult(result),
                "timestamp", LocalDateTime.now()
        ));


    }

    private ErrorTestRequest checkAndLogErrorTestRequest(ErrorTestRequest errorTestRequest, String strategy, String configuration) {
        ErrorTestRequest verifiedErrorTestRequest = errorTestRequest == null ? createDefaultErrorRequest() : errorTestRequest;

        logger.info("=== Testing {} Retry Strategy ===", strategy);
        switch (strategy) {
            case "PROGRAMMATIC":
                logger.info("Configuration: Feign Builder + Custom RetryConfig");
                break;
            case "ANNOTATION":
                logger.info("Configuration: @Retry Annotation + YAML Config");
                break;
            default:
                logger.warn("Unknown strategy '{}' configuration", strategy);
        }

        logger.info("Error request: {}", verifiedErrorTestRequest);

        switch (strategy) {
            case "PROGRAMMATIC":
                logger.info("👨‍💻 [{}-RETRY] Starting call with FEIGN BUILDER + RetryConfig", strategy);
                break;
            case "ANNOTATION":
                logger.info("＠ [{}-RETRY] Starting call with @RETRY ANNOTATION + YAML configuration", strategy);
                break;
            default:
                logger.warn("Unknown Starting strategy '{}'", strategy);
        }
        logger.info("⚙️ [{}-RETRY] Error config: errorCode={}, errorRate={}, delay={}ms",
                verifiedErrorTestRequest.getErrorCode(), verifiedErrorTestRequest.getErrorRate(), verifiedErrorTestRequest.getResponseDelayMs(), strategy);
        return verifiedErrorTestRequest;
    }

    /**
     * Build programmatic strategy description from configuration
     */
    private String buildProgrammaticStrategyDescription() {
        return String.format("Feign Builder - %s",
                programmaticRetryConfig.getStrategy().replace("-", " ").toUpperCase());
    }

    /**
     * Build programmatic configuration description from YAML values
     */
    private String buildProgrammaticConfigurationDescription() {
        if ("random-backoff".equals(programmaticRetryConfig.getStrategy())) {
            return String.format("%d attempts, exponential random backoff (%dms-%ds, multiplier: %.1f, randomization: %.1f)",
                    programmaticRetryConfig.getMaxAttempts(),
                    programmaticRetryConfig.getInitialInterval(),
                    programmaticRetryConfig.getMaxInterval() / 1000,
                    programmaticRetryConfig.getMultiplier(),
                    programmaticRetryConfig.getRandomizationFactor());
        } else {
            return String.format("%d attempts, exponential backoff (%dms, multiplier: %.1f)",
                    programmaticRetryConfig.getMaxAttempts(),
                    programmaticRetryConfig.getInitialInterval(),
                    programmaticRetryConfig.getMultiplier());
        }
    }

    /**
     * Build annotation configuration description from YAML values
     */
    private String buildAnnotationConfigurationDescription() {
        return String.format("%d attempts, exponential backoff (%s with %.1fx multiplier)",
                annotationMaxAttempts,
                annotationWaitDuration,
                annotationMultiplier);
    }

    /**
     * Format the result to be more readable in Bruno GUI
     */
    private Object formatResult(String result) {
        if (result == null || result.trim().isEmpty()) {
            return "Empty response";
        }

        try {
            if (result.trim().startsWith("{") || result.trim().startsWith("[")) {
                return objectMapper.readValue(result, Object.class);
            }
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse result as JSON: {}", e.getMessage());
        }

        Map<String, Object> formattedResult = new LinkedHashMap<>();
        formattedResult.put("message", result);
        formattedResult.put("type", "text/plain");
        formattedResult.put("length", result.length());

        return formattedResult;
    }

    /**
     * This default was used before for fast test can be removed when the simulation stable
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