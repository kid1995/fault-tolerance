package com.example.resilience_app.utils;

import io.github.resilience4j.retry.event.RetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RetryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(RetryEventListener.class);

    /**
     * Handle retry events and log them with detailed information
     */
    public static void onRetryEvent(RetryEvent retryEvent) {
        switch (retryEvent.getEventType()) {
            case RETRY:
                handleRetryAttempt(retryEvent);
                break;
            case SUCCESS:
                handleSuccess(retryEvent);
                break;
            case ERROR:
                handleRetryError(retryEvent);
                break;
            case IGNORED_ERROR:
                handleIgnoredError(retryEvent);
                break;
            default:
                logger.debug("🔍 [UNKNOWN-EVENT] Name: {} | Event Type: {} | Attempts: {}",
                        retryEvent.getName(),
                        retryEvent.getEventType(),
                        retryEvent.getNumberOfRetryAttempts());
                break;
        }
    }

    private static void handleRetryAttempt(RetryEvent retryEvent) {
        logger.warn("🔄 [RETRY-ATTEMPT] Name: {} | Attempt: {} | Exception: {} ",
                retryEvent.getName(),
                retryEvent.getNumberOfRetryAttempts(),
                retryEvent.getLastThrowable().getClass().getSimpleName()
        );
        logger.warn("📧 [RETRY-ATTEMPT] Message: {}", retryEvent.getLastThrowable().getMessage());
    }

    private static void handleSuccess(RetryEvent retryEvent) {
        if (retryEvent.getNumberOfRetryAttempts() == 0) {
            logger.info("✅ [FIRST-ATTEMPT-SUCCESS] Name: {} | Succeeded on first attempt",
                    retryEvent.getName());
        } else {
            logger.info("✅ [RETRY-SUCCESS] Name: {} | Success after {} attempts",
                    retryEvent.getName(),
                    retryEvent.getNumberOfRetryAttempts());
        }
    }

    private static void handleRetryError(RetryEvent retryEvent) {
        logger.error("❌ [RETRY-EXHAUSTED] Name: {} | All {} attempts failed | Final Exception: {}",
                retryEvent.getName(),
                retryEvent.getNumberOfRetryAttempts(),
                retryEvent.getLastThrowable().getClass().getSimpleName())
        ;
        logger.warn("❌ [RETRY-EXHAUSTED] Message: {}", retryEvent.getLastThrowable().getMessage());
    }

    private static void handleIgnoredError(RetryEvent retryEvent) {
        logger.debug("⚠️ [RETRY-IGNORED] Name: {} | Exception ignored (not retryable): {}",
                retryEvent.getName(),
                retryEvent.getLastThrowable().getClass().getSimpleName()
        );
        logger.warn("Message: {}", retryEvent.getLastThrowable().getMessage());
    }
}