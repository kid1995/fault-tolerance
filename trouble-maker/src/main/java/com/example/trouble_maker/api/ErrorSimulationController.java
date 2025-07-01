package com.example.trouble_maker.api;

import com.example.trouble_maker.model.ErrorConfig;
import com.example.trouble_maker.service.ErrorConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/errors")
public class ErrorSimulationController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorSimulationController.class);

    private final ErrorConfigurationService errorConfigService;

    public ErrorSimulationController(ErrorConfigurationService errorConfigService) {
        this.errorConfigService = errorConfigService;
    }

    /**
     * 503 Service Unavailable
     * Simulates temporary overload or scheduled maintenance
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> serviceUnavailable(
            @RequestParam Map<String, String> requestParams,
            @RequestBody ErrorConfig errorConfig) {
        if (!requestParams.containsKey("errorCode")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required parameter: errorCode"));
        }
        String errorCode = requestParams.get("errorCode");


        HttpStatus httpErrorStatus = errorConfigService.getErrorCode(errorCode);
        logger.info("🎲 Using error rate to random if error should be simulated or return succes: {}", errorConfig.getErrorRate());
        if (errorConfigService.shouldSimulateError(errorConfig)) {
            logger.info("❌ Simulating error code {} - config: {}",errorCode, errorConfig);
            errorConfigService.simulateDelay(errorConfig);

            Map<String, Object> errorResponse = Map.of(
                    "error", httpErrorStatus.value(),
                    "message", httpErrorStatus.getReasonPhrase(),
                    "timestamp", LocalDateTime.now(),
                    "retryAfter", errorConfig.getRetryAfterSeconds()
            );

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Retry-After", String.valueOf(errorConfig.getRetryAfterSeconds()))
                    .body(errorResponse);
        }
        logger.info("✅ Simulating success response - no error");
        return successResponse("service available - no error");
    }

    private ResponseEntity<Map<String, Object>> successResponse(String message) {
        Map<String, Object> response = Map.of(
                "status", "success",
                "message", message,
                "timestamp", LocalDateTime.now(),
                "data", "Resource successfully retrieved"
        );
        return ResponseEntity.ok(response);
    }
}