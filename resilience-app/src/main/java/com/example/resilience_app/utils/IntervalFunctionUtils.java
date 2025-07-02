package com.example.resilience_app.utils;

import io.github.resilience4j.core.IntervalFunction;
import java.time.Duration;
import java.util.Random;

public final class IntervalFunctionUtils {

    private static final Random random = new Random();
    private IntervalFunctionUtils() {}

    public static IntervalFunction fixed(Duration interval) {
        return attempt -> interval.toMillis();
    }

    public static IntervalFunction linear(Duration baseInterval) {
        return attempt -> baseInterval.toMillis() * attempt;
    }



    // This function implements an exponential backoff strategy with jitter.
    // This is used to handle retries in a distributed system, particularly to mitigate the "thundering herd" problem.
    // The thundering herd problem occurs when many clients retry simultaneously, overwhelming the server.

    // Randomized backoff, introduced by jitter, helps distribute the retry attempts over time.

    // Resilience4j already provides `IntervalFunction.ofExponentialRandomBackoff` for this purpose.
    // This code serves as a demonstration and allows customization of the jitter randomization.

    public static IntervalFunction exponentialWithJitter(Duration baseInterval, double multiplier,
                                                         double jitterFactor, Duration maxInterval) {
        return attempt -> {
            // Calculate exponential delay based on the attempt number.
            // The delay increases exponentially with each subsequent attempt.
            long baseDelay = (long) (baseInterval.toMillis() * Math.pow(multiplier, (double) attempt - 1));

            // Cap the delay to the specified maximum interval.
            long cappedDelay = Math.min(baseDelay, maxInterval.toMillis());

            // Introduce jitter to randomize the delay.
            // jitterFactor controls the range of randomization.
            double jitter = random.nextDouble() * jitterFactor;

            // Apply jitter to the capped delay.
            long jitterDelay = (long) (cappedDelay * (1 + jitter));

            // Ensure the delay is non-negative.
            return Math.max(jitterDelay, 0);
        };
    }


    public static class Presets {
        public static final IntervalFunction STANDARD_RETRY = linear(Duration.ofSeconds(1));

        private Presets() {}
    }
}
