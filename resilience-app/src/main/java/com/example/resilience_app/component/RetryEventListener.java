package com.example.resilience_app.component;

import io.github.resilience4j.retry.event.RetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple retry event listener that logs all retry events
 * for monitoring and debugging purposes.
 */
@Component
public class RetryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(RetryEventListener.class);

    /**
     * Handle retry events and log them with detailed information
     */
    public void onRetryEvent(RetryEvent retryEvent) {
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

    private void handleRetryAttempt(RetryEvent retryEvent) {
        logger.warn("🔄 [RETRY-ATTEMPT] Name: {} | Attempt: {} | Exception: {} | Message: {}",
                retryEvent.getName(),
                retryEvent.getNumberOfRetryAttempts(),
                retryEvent.getLastThrowable().getClass().getSimpleName(),
                retryEvent.getLastThrowable().getMessage());
    }

    private void handleSuccess(RetryEvent retryEvent) {
        if (retryEvent.getNumberOfRetryAttempts() == 0) {
            logger.info("✅ [FIRST-ATTEMPT-SUCCESS] Name: {} | Succeeded on first attempt",
                    retryEvent.getName());
        } else {
            logger.info("✅ [RETRY-SUCCESS] Name: {} | Success after {} attempts",
                    retryEvent.getName(),
                    retryEvent.getNumberOfRetryAttempts());
        }
    }

    private void handleRetryError(RetryEvent retryEvent) {
        logger.error("❌ [RETRY-EXHAUSTED] Name: {} | All {} attempts failed | Final Exception: {} | Message: {}",
                retryEvent.getName(),
                retryEvent.getNumberOfRetryAttempts(),
                retryEvent.getLastThrowable().getClass().getSimpleName(),
                retryEvent.getLastThrowable().getMessage());
    }

    private void handleIgnoredError(RetryEvent retryEvent) {
        logger.debug("⚠️ [RETRY-IGNORED] Name: {} | Exception ignored (not retryable): {} | Message: {}",
                retryEvent.getName(),
                retryEvent.getLastThrowable().getClass().getSimpleName(),
                retryEvent.getLastThrowable().getMessage());
    }
}