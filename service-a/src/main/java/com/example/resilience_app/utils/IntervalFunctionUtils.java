package com.example.service_a.utils;

import io.github.resilience4j.core.IntervalFunction;
import java.time.Duration;

public final class IntervalFunctionUtils {

    private IntervalFunctionUtils() {}

    public static IntervalFunction fixed(Duration interval) {
        return attempt -> interval.toMillis();
    }

    public static IntervalFunction linear(Duration baseInterval) {
        return attempt -> baseInterval.toMillis() * attempt;
    }

    public static IntervalFunction quadratic(Duration baseInterval) {
        return attempt -> baseInterval.toMillis() * attempt * attempt;
    }

    public static IntervalFunction cappedExponential(Duration baseInterval, double multiplier, Duration maxInterval) {
        return attempt -> {
            long exponentialDelay = (long) (baseInterval.toMillis() * Math.pow(multiplier, attempt - 1));
            return Math.min(exponentialDelay, maxInterval.toMillis());
        };
    }

    public static IntervalFunction exponentialWithJitter(Duration baseInterval, double multiplier,
                                                         double jitterFactor, Duration maxInterval) {
        return attempt -> {
            long baseDelay = (long) (baseInterval.toMillis() * Math.pow(multiplier, attempt - 1));
            long cappedDelay = Math.min(baseDelay, maxInterval.toMillis());
            double jitter = (Math.random() - 0.5) * 2 * jitterFactor;
            long jitteredDelay = (long) (cappedDelay * (1 + jitter));
            return Math.max(jitteredDelay, 0);
        };
    }

    public static IntervalFunction businessFriendly() {
        return attempt -> {
            if (attempt <= 2) {
                return Duration.ofMillis(500).toMillis();
            } else if (attempt <= 4) {
                return Duration.ofSeconds(2).toMillis();
            } else if (attempt <= 6) {
                return Duration.ofSeconds(10).toMillis();
            } else {
                return Duration.ofSeconds(30).toMillis();
            }
        };
    }

    public static IntervalFunction rateLimitFriendly() {
        return attempt -> {
            return Duration.ofSeconds(5L * attempt).toMillis();
        };
    }

    public static IntervalFunction databaseFriendly() {
        return attempt -> {
            if (attempt == 1) {
                return Duration.ofMillis(100).toMillis();
            } else if (attempt <= 3) {
                return Duration.ofMillis(500).toMillis();
            } else {
                return Duration.ofSeconds(2L * (attempt - 2)).toMillis();
            }
        };
    }

    public static class Presets {
        public static final IntervalFunction QUICK_RETRY = fixed(Duration.ofMillis(100));
        public static final IntervalFunction STANDARD_RETRY = linear(Duration.ofSeconds(1));
        public static final IntervalFunction SLOW_RETRY = exponentialWithJitter(
                Duration.ofSeconds(2), 2.0, 0.1, Duration.ofSeconds(30));
        public static final IntervalFunction API_RATE_LIMIT = rateLimitFriendly();
        public static final IntervalFunction DATABASE_RETRY = databaseFriendly();
        public static final IntervalFunction BUSINESS_RETRY = businessFriendly();
    }
}
