package com.example.resilience_app.utils;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Set;

public final class RetryConfigUtil {

    private RetryConfigUtil() {
    }

    public static RetryConfig createStandardRetry(int maxAttempts,
                                                  long initialDelay, // in milliseconds
                                                  Double multiplier,
                                                  Set<Class<? extends Throwable>> retryExceptions ) {
        return RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(initialDelay), multiplier))
                .retryExceptions(
                        java.net.ConnectException.class,           // Connection refused
                        java.net.UnknownHostException.class,       // DNS resolution failures
                        java.io.IOException.class,                 // General I/O errors

                        // Feign specific exceptions
                        feign.RetryableException.class,            // Feign retryable errors
                        feign.FeignException.class              // All Feign exceptions
                )
                .build();
    }

    public static RetryConfig createRandomBackoffRetry(
            int maxAttempts,
            long initialInterval,
            double multiplier,
            double randomizationFactor,
            long maxInterval
    ) {
        return RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(
                        Duration.ofMillis(initialInterval),  // initial interval
                        multiplier,                     // multiplier
                        randomizationFactor,                     // randomization factor
                        Duration.ofSeconds(maxInterval)   // max interval
                ))
                .retryExceptions(
                        java.net.ConnectException.class,           // Connection refused
                        java.net.UnknownHostException.class,       // DNS resolution failures
                        java.io.IOException.class,                 // General I/O errors

                        // Feign specific exceptions
                        feign.RetryableException.class,            // Feign retryable errors
                        feign.FeignException.class                // All Feign exceptions
                )
                .build();
    }

    public static RetryConfig createCustomBackoff() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(createCustomLinearInterval())
                .retryExceptions(
                        java.net.ConnectException.class,           // Connection refused
                        java.net.UnknownHostException.class,       // DNS resolution failures
                        java.io.IOException.class,                 // General I/O errors

                        // Feign specific exceptions
                        feign.RetryableException.class,            // Feign retryable errors
                        feign.FeignException.class                // All Feign exceptions
                )
                .build();
    }

    private static IntervalFunction createCustomLinearInterval() {
        return IntervalFunctionUtils.Presets.STANDARD_RETRY;
    }
    
}