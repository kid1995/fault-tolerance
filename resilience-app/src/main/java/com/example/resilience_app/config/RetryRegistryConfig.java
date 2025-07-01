package com.example.resilience_app.config;

import com.example.resilience_app.utils.RetryEventListener;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class RetryRegistryConfig {

    private static final Logger logger = LoggerFactory.getLogger(RetryRegistryConfig.class);
    private final RetryRegistry retryRegistry;

    public RetryRegistryConfig(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void setupRetryEventListeners() {
        logger.info("🚀 [RETRY_REGISTRY_CONFIG] Setting up event listeners for YAML-configured retry instances...");

        // Listen to all retry events globally
        retryRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    Retry retry = entryAddedEvent.getAddedEntry();
                    String retryName = retry.getName();
                    logger.info("➕ [RETRY_REGISTRY_CONFIG] New retry instance registered: {}", retryName);
                    // Attach event listener to the new retry instance
                    retry.getEventPublisher().onEvent(RetryEventListener::onRetryEvent);

                })
                .onEntryRemoved(entryRemovedEvent -> {
                    String retryName = entryRemovedEvent.getRemovedEntry().getName();
                    logger.info("➖ [RETRY_REGISTRY_CONFIG] Retry instance removed: {}", retryName);
                });
    }
}