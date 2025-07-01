package com.example.resilience_app.config.service;

import com.example.resilience_app.adapter.http.client.ProgrammaticRetryClient;
import com.example.resilience_app.adapter.http.client.ProgrammaticRetryFallBack;
import com.example.resilience_app.model.ErrorTestRequest;
import com.example.resilience_app.utils.RetryConfigUtil;
import com.example.resilience_app.utils.RetryEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.codec.Decoder;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.LocalDateTime;

@Configuration
public class ProgrammaticRetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProgrammaticRetryConfig.class);
    private final static String clientName = "programmaticRetry";

    @Value("${app.troubleMaker.url}")
    private String troubleMakerURL;

    @Value("${app.programmaticRetryConfig.max-attempts}")
    private int maxAttempts;

    @Value("${app.programmaticRetryConfig.strategy}")
    private String strategy;

    @Value("${app.programmaticRetryConfig.initial-interval}")
    private long initialInterval;

    @Value("${app.programmaticRetryConfig.exponential-backoff-multiplier}")
    private double multiplier;

    @Value("${app.programmaticRetryConfig.randomization-factor}")
    private double randomizationFactor;

    @Value("${app.programmaticRetryConfig.maxInterval}")
    private long maxInterval;

    private final RetryRegistry retryRegistry;

    public ProgrammaticRetryConfig(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @Bean
    public Retry programmaticRetry() {
        logger.info("🔧 [PROGRAMMATIC-RETRY] Creating retry instance: {}", clientName);
        logger.info("🔧 [PROGRAMMATIC-RETRY] Strategy: {}, Max Attempts: {}, Initial Interval: {}ms, Multiplier: {}, Randomization Factor: {}, Max Interval: {}ms",
                strategy, maxAttempts, initialInterval, multiplier, randomizationFactor, maxInterval);

        RetryConfig retryConfig = switch (strategy.toLowerCase()) {
            case "random-backoff" -> RetryConfigUtil.createRandomBackoffRetry(
                    maxAttempts,
                    initialInterval,
                    multiplier,
                    randomizationFactor,
                    maxInterval / 1000 // Convert to seconds for maxInterval
            );
            case "standard-exponential" -> RetryConfigUtil.createStandardRetry(
                    maxAttempts,
                    initialInterval / 1000, // Convert to seconds
                    multiplier,
                    null
            );
            default -> {
                logger.warn("Unknown strategy '{}', falling back to random-backoff", strategy);
                yield RetryConfigUtil.createRandomBackoffRetry(
                        maxAttempts,
                        initialInterval,
                        multiplier,
                        randomizationFactor,
                        maxInterval / 1000
                );
            }
        };

        // Create a retry instance and register it with RetryRegistry
        Retry retry = retryRegistry.retry(clientName, retryConfig);
        retry.getEventPublisher().onEvent(RetryEventListener::onRetryEvent);
        logger.info("✅ [PROGRAMMATIC-RETRY] Retry instance '{}' registered with RetryRegistry", clientName);
        return retry;
    }

    /**
     * Spring Contract Bean - WICHTIG für @PostMapping Support!
     */
    @Bean
    public SpringMvcContract springContract() {
        return new SpringMvcContract();
    }

    @Bean
    SpringEncoder feignEncoder() {
        var jsonMessageConverters = new MappingJackson2HttpMessageConverter(new ObjectMapper());
        return new SpringEncoder(() -> new HttpMessageConverters(jsonMessageConverters));
    }

    @Bean
    Decoder feignDecoder() {
        var jsonMessageConverters = new MappingJackson2HttpMessageConverter(new ObjectMapper());
        return new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(jsonMessageConverters)));
    }

    @Bean("programmaticRetryClientBean")
    public ProgrammaticRetryClient programmaticRetryClient(Retry programmaticRetry, SpringMvcContract springContract) {
        FeignDecorators decorators = FeignDecorators.builder()
                .withRetry(programmaticRetry)
                .withFallbackFactory(ProgrammaticRetryFallBack::new)
                .build();

        return Feign.builder()
                .addCapability(Resilience4jFeign.capability(decorators))
                .encoder(feignEncoder())
                .decoder(feignDecoder())
                .contract(springContract)
                .target(ProgrammaticRetryClient.class, troubleMakerURL);
    }

    // Getter methods for use in other components
    public int getMaxAttempts() {
        return maxAttempts;
    }

    public String getStrategy() {
        return strategy;
    }

    public long getInitialInterval() {
        return initialInterval;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public double getRandomizationFactor() {
        return randomizationFactor;
    }

    public long getMaxInterval() {
        return maxInterval;
    }
}