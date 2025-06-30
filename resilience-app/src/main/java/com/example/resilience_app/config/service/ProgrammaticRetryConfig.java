package com.example.resilience_app.config.service;

import com.example.resilience_app.adapter.http.client.ProgrammaticRetryClient;
import com.example.resilience_app.utils.RetryConfigUtil;
import feign.Feign;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProgrammaticRetryConfig {

    @Value("${app.troubleMaker.url}")
    private String troubleMakerURL;

    private final static String clientName = "programmaticRetryClient";

    @Bean
    public Retry programmaticRetry() {
        RetryConfig retryConfig = RetryConfigUtil.createDatabaseRetry();
        return Retry.of(clientName, retryConfig);
    }

    /**
     * Spring Contract Bean - WICHTIG für @GetMapping Support!
     */
    @Bean
    public SpringMvcContract springContract() {
        return new SpringMvcContract();
    }

    @Bean("programmaticRetryClientBean")  // Give it a specific name
    public ProgrammaticRetryClient programmaticRetryClient(Retry programmaticRetry, SpringMvcContract springContract) {
        FeignDecorators decorators = FeignDecorators.builder()
                .withRetry(programmaticRetry)
                .build();

        return Feign.builder()
                .addCapability(Resilience4jFeign.capability(decorators))
                .contract(springContract)  // Use SpringMvcContract for @GetMapping support
                .target(ProgrammaticRetryClient.class, troubleMakerURL);
    }
}