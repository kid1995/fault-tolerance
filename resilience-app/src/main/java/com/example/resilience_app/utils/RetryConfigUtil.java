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

    public static RetryConfig createStandardRetry() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(1), 2.0))
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

    public static RetryConfig createRandomBackoffRetry() {
        return RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(
                        Duration.ofMillis(200),  // initial interval
                        2.0,                     // multiplier
                        0.1,                     // randomization factor
                        Duration.ofSeconds(10)   // max interval
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