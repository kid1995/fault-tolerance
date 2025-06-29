package com.example.service_a.utils;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;

public final class RetryConfigUtil {

    private RetryConfigUtil() {
    }

    public static RetryConfig createStandardRetry() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(1), 2.0))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();
    }

    public static RetryConfig createFastRetry() {
        return RetryConfig.custom()
                .maxAttempts(2)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(500), 1.5))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();
    }

    public static RetryConfig createRobustRetry() {
        return RetryConfig.custom()
                .maxAttempts(5)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(2), 2.5))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();
    }

    public static RetryConfig createGentleRetry() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.of(Duration.ofSeconds(5))) // Fixed interval
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();
    }

    public static RetryConfig createDatabaseRetry() {
        return RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(
                        Duration.ofMillis(200),  // initial interval
                        2.0,                     // multiplier
                        0.1,                     // randomization factor
                        Duration.ofSeconds(10)   // max interval
                ))
                .retryExceptions(
                        java.sql.SQLException.class,
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class
                )
                .build();
    }

    public static RetryConfig createRateLimitRetry() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(createCustomLinearInterval())
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();
    }

    private static IntervalFunction createCustomLinearInterval() {
        return IntervalFunctionUtils.Presets.STANDARD_RETRY;
    }
    
}