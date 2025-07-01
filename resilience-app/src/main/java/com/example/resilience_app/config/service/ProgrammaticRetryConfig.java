package com.example.resilience_app.config.service;

import com.example.resilience_app.adapter.http.client.ProgrammaticRetryClient;
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

@Configuration
public class ProgrammaticRetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProgrammaticRetryConfig.class);

    @Value("${app.troubleMaker.url}")
    private String troubleMakerURL;

    private final static String clientName = "programmaticRetryClient";

    private final RetryRegistry retryRegistry;

    public ProgrammaticRetryConfig(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }

    @Bean
    public Retry programmaticRetry() {
        logger.info("🔧 [PROGRAMMATIC-RETRY] Creating retry instance: {}", clientName);

        RetryConfig retryConfig = RetryConfigUtil.createRandomBackoffRetry();

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

    @Bean("programmaticRetryClientBean")  // Give it a specific name
    public ProgrammaticRetryClient programmaticRetryClient(Retry programmaticRetry, SpringMvcContract springContract) {
        FeignDecorators decorators = FeignDecorators.builder()
                .withRetry(programmaticRetry)
                .build();

        return Feign.builder()
                .addCapability(Resilience4jFeign.capability(decorators))
                .encoder(feignEncoder())
                .decoder(feignDecoder())
                .contract(springContract)  // Use SpringMvcContract for @GetMapping support
                .target(ProgrammaticRetryClient.class, troubleMakerURL);
    }
}