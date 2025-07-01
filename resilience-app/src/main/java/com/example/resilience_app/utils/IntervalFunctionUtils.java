package com.example.resilience_app.utils;

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

    public static IntervalFunction exponentialWithJitter(Duration baseInterval, double multiplier,
                                                         double jitterFactor, Duration maxInterval) {
        return attempt -> {
            long baseDelay = (long) (baseInterval.toMillis() * Math.pow(multiplier, attempt - 1));
            long cappedDelay = Math.min(baseDelay, maxInterval.toMillis());
            double jitter = (Math.random() - 0.5) * 2 * jitterFactor;
            long jitterDelay = (long) (cappedDelay * (1 + jitter));
            return Math.max(jitterDelay, 0);
        };
    }


    public static class Presets {
        public static final IntervalFunction STANDARD_RETRY = linear(Duration.ofSeconds(1));
    }
}
