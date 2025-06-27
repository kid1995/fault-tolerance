package com.example.client_a.config.service;

import com.example.client_a.adapter.http.client.ServiceBClient;
import com.example.client_a.config.FeignConfig;
import com.example.client_a.config.RetryConfigRegistry;
import feign.Feign;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import(FeignConfig.class)
public class FeignConfig4ServiceB {

    @Value("${spring.cloud.openfeign.client.config.serviceB.url}")
    private String serviceBUrl;

    private final static String clientName = "serviceBClient";

    private final RetryRegistry retryRegistry;

    public FeignConfig4ServiceB(RetryRegistry retryRegistry) {
        this.retryRegistry = retryRegistry;
    }


    @Bean
    public Retry retry() {
        return retryRegistry.retry(clientName, RetryConfigRegistry.RetryConfigNames.FAST);
    }

    @Bean
    @Primary
    public ServiceBClient serviceBClient(Retry retry) {
        FeignDecorators decorators = FeignDecorators.builder()
                .withRetry(retry)
                .build();

        return Feign.builder()
                .addCapability(new Resilience4jFeign.Capability(decorators))
                .target(ServiceBClient.class, serviceBUrl);
    }
}