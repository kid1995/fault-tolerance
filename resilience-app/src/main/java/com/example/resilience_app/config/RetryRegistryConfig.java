package com.example.resilience_app.config;

import com.example.resilience_app.component.RetryEventListener;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import jakarta.annotation.PostConstruct;

@Configuration
public class RetryRegistryConfig {

    private static final Logger logger = LoggerFactory.getLogger(RetryRegistryConfig.class);

    private final RetryEventListener retryEventListener;
    private final RetryRegistry retryRegistry;

    public RetryRegistryConfig(RetryEventListener retryEventListener, RetryRegistry retryRegistry) {
        this.retryEventListener = retryEventListener;
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void setupRetryEventListeners() {
        // Listen to all retry events globally
        retryRegistry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    Retry retry = entryAddedEvent.getAddedEntry();
                    String retryName = retry.getName();
                    logger.info("➕ [RETRY_REGISTRY_CONFIG] New retry instance registered: {}", retryName);
                    // Attach event listener to the new retry instance
                    retry.getEventPublisher().onEvent(retryEventListener::onRetryEvent);

                })
                .onEntryRemoved(entryRemovedEvent -> {
                    String retryName = entryRemovedEvent.getRemovedEntry().getName();
                    logger.info("➖ [RETRY_REGISTRY_CONFIG] Retry instance removed: {}", retryName);
                });
    }

    /**
     * Bean to expose RetryEventListener for monitoring endpoints
     */
    @Bean
    public RetryEventListener retryEventListener() {
        return retryEventListener;
    }

    /**
     * Spring event listener to handle application-level retry events
     * This catches any retry events published as Spring events
     */
    @EventListener
    public void handleRetryEvent(RetryEvent retryEvent) {
        // This will catch any retry events published as Spring events
        retryEventListener.onRetryEvent(retryEvent);
    }
}