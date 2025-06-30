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
    @GetMapping
    public ResponseEntity<Map<String, Object>> serviceUnavailable(
            @RequestParam Map<String, String> requestParams,
            @RequestBody ErrorConfig errorConfig) {
        if (!requestParams.containsKey("errorName")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required parameter: errorName"));
        }
        String errorCode = requestParams.get("errorName");
        logger.info("Simulating error code {} - config: {}",errorCode, errorConfig.toString());

        HttpStatus httpErrorStatus = errorConfigService.getErrorCode(errorCode);

        if (errorConfigService.shouldSimulateError(errorConfig)) {
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