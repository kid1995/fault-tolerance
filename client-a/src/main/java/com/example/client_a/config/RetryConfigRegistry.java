package com.example.client_a.config;

import com.example.client_a.utils.IntervalFunctionUtils;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RetryConfigRegistry {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();

        // Standard retry - exponential backoff
        RetryConfig standardRetry = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(1), 2.0))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();

        // Fast retry - shorter exponential backoff
        RetryConfig fastRetry = RetryConfig.custom()
                .maxAttempts(2)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(500), 1.5))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();

        // Robust retry - longer exponential backoff
        RetryConfig robustRetry = RetryConfig.custom()
                .maxAttempts(5)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(2), 2.5))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();

        // Gentle retry - fixed interval (no backoff)
        RetryConfig gentleRetry = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.of(Duration.ofSeconds(5))) // Fixed interval
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();

        // Database retry - exponential with randomization
        RetryConfig databaseRetry = RetryConfig.custom()
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

        // API rate limit retry - custom interval function
        RetryConfig rateLimitRetry = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(createCustomLinearInterval(Duration.ofSeconds(5)))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class,
                        feign.RetryableException.class
                )
                .build();

        // Register all configurations
        registry.retry("standard", standardRetry);
        registry.retry("fast", fastRetry);
        registry.retry("robust", robustRetry);
        registry.retry("gentle", gentleRetry);
        registry.retry("database", databaseRetry);
        registry.retry("rateLimit", rateLimitRetry);

        return registry;
    }

    // Custom linear interval function (since ofLinearBackoff doesn't exist)
    private IntervalFunction createCustomLinearInterval(Duration baseInterval) {
        return IntervalFunctionUtils.Presets.STANDARD_RETRY;
    }

    public static final class RetryConfigNames {
        public static final String STANDARD = "standard";
        public static final String FAST = "fast";
        public static final String ROBUST = "robust";
        public static final String GENTLE = "gentle";
        public static final String DATABASE = "database";
        public static final String RATE_LIMIT = "rateLimit";
    }
}