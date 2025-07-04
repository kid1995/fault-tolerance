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
     * Uses Feign Builder with custom retry configuration
     */
    @PostMapping("/programmatic/retry")
    public ResponseEntity<Map<String, Object>> testProgrammaticRetry(@RequestBody(required = false) ErrorTestRequest errorRequest) {
        ErrorTestRequest verifiedErrorTestRequest = checkAndLogErrorTestRequest(errorRequest, "PROGRAMMATIC", "Feign Builder + Custom RetryConfig");

        String strategyDescription = getProgrammaticStrategyDescription();
        String configurationDescription = getProgrammaticConfigurationDescription();

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

    @PostMapping("/programmatic/retry/after-consume-kafka-msg/{consumeWay}/{numOfMessages}")
    public ResponseEntity<Map<String, Object>> testSequentialBatchWindow(
            @PathVariable("numOfMessages") int numOfMessages,  // ✅ Fixed: Use @PathVariable
            @PathVariable("consumeWay") String consumeWay,     // ✅ Fixed: Use @PathVariable
            @RequestBody(required = false) ErrorTestRequest errorRequest) {

        ErrorTestRequest verifiedErrorTestRequest = checkAndLogErrorTestRequest(errorRequest, "BATCH-WINDOW", "Consumer + Window Batch");

        logger.info("🚀 [KAFKA] Processing {} messages with {} strategy", numOfMessages, consumeWay);

        switch (consumeWay.toLowerCase()) {
            case "sequential":
                logger.info("📦 [KAFKA] Using SEQUENTIAL consume strategy");
                messageProducer.sendSequentialMessages(numOfMessages, verifiedErrorTestRequest);
                break;
            case "concurrent":
                logger.info("📦 [KAFKA] Using CONCURRENT consume strategy");
                messageProducer.sendConcurrentMessages(numOfMessages, verifiedErrorTestRequest);
                break;
            default:
                logger.warn("⚠️ [KAFKA] Unknown consume strategy: {}. Defaulting to SEQUENTIAL.", consumeWay);
                messageProducer.sendSequentialMessages(numOfMessages, verifiedErrorTestRequest);
                break;
        }

        return ResponseEntity.ok(
                Map.of(
                        "status", "SUCCESS",
                        "strategy", "BATCH-WINDOW-" + consumeWay.toUpperCase(),
                        "configuration", String.format("Batch size: %d, Consume way: %s", numOfMessages, consumeWay),
                        "result", "Messages sent successfully",
                        "messagesCount", numOfMessages,
                        "timestamp", LocalDateTime.now()
                )
        );
    }

    private ErrorTestRequest checkAndLogErrorTestRequest(ErrorTestRequest errorTestRequest, String strategy, String configuration) {
        ErrorTestRequest verifiedErrorTestRequest = errorTestRequest == null ? createDefaultErrorRequest() : errorTestRequest;

        logger.info("=== Testing {} Strategy ===", strategy);
        logger.info("Configuration: {}", configuration);
        logger.info("Error request: {}", verifiedErrorTestRequest);
        logger.info("⚙️ [{}] Error config: errorCode={}, errorRate={}, delay={}ms",
                strategy, verifiedErrorTestRequest.getErrorCode(),
                verifiedErrorTestRequest.getErrorRate(), verifiedErrorTestRequest.getResponseDelayMs());
        return verifiedErrorTestRequest;
    }

    private String getProgrammaticStrategyDescription() {
        return String.format("Feign Builder - %s",
                programmaticRetryConfig.getStrategy().replace("-", " ").toUpperCase());
    }

    private String getProgrammaticConfigurationDescription() {
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

    private String buildAnnotationConfigurationDescription() {
        return String.format("%d attempts, exponential backoff (%s with %.1fx multiplier)",
                annotationMaxAttempts,
                annotationWaitDuration,
                annotationMultiplier);
    }

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