package com.example.resilience_app.config.service;

import com.example.resilience_app.utils.RetryEventListener;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotationRetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationRetryConfig.class);

    private final RetryRegistry retryRegistry;

    public AnnotationRetryConfig(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @Bean
    public Retry annotationRetryConfigBean() {
        logger.info("🔧 [ANNOTATION-RETRY] Setting up event listener for annotationRetryConfig");

        // Get the retry instance that Resilience4j creates automatically from YAML config
        Retry retry = retryRegistry.retry("annotationRetryConfig");

        // Add our custom event listener to get the same logging as programmatic retry
        retry.getEventPublisher().onEvent(RetryEventListener::onRetryEvent);

        logger.info("✅ [ANNOTATION-RETRY] Event listener registered for annotationRetryConfig");
        return retry;
    }
}