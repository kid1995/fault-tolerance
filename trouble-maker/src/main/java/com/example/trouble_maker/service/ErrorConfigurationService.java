package com.example.trouble_maker.service;

import com.example.trouble_maker.model.ErrorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ErrorConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ErrorConfigurationService.class);


    public HttpStatus getErrorCode(String errorCode) {
        return switch (errorCode) {
            case "503" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "500" -> HttpStatus.INTERNAL_SERVER_ERROR;
            case "502" -> HttpStatus.BAD_GATEWAY;
            case "504" -> HttpStatus.GATEWAY_TIMEOUT;
            case "429" -> HttpStatus.TOO_MANY_REQUESTS;
            case "408" -> HttpStatus.REQUEST_TIMEOUT;
            default -> {
                logger.warn("Unknown error code: {}", errorCode);
                yield HttpStatus.INTERNAL_SERVER_ERROR;
            }
        };
    }

    // Helper methods
    public boolean shouldSimulateError(ErrorConfig config) {
        return ThreadLocalRandom.current().nextDouble() < config.getErrorRate();
    }

    public void simulateDelay(ErrorConfig config) {
        int delay = config.getResponseDelayMs();
        if (delay > 0) {
            try {
                logger.debug("Simulating response delay: {}ms", delay);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}