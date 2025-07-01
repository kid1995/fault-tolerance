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
    public Retry annotationRetry() {
        logger.info("🔧 [ANNOTATION-RETRY] Creating retry instance from YAML config: annotationRetryConfig");

        // Get the retry instance from the registry (created from YAML)
        Retry retry = retryRegistry.retry("annotationRetryConfig");

        // Attach event listener directly
        retry.getEventPublisher().onEvent(RetryEventListener::onRetryEvent);

        logger.info("✅ [ANNOTATION-RETRY] Retry instance 'annotationRetryConfig' loaded from YAML");
        logger.info("🔗 [ANNOTATION-RETRY] Event listener attached to retry instance 'annotationRetryConfig'");

        return retry;
    }
}