package com.example.service_a.config.service;

import com.example.service_a.adapter.http.client.ServiceBClient;
import com.example.service_a.config.FeignConfig;
import com.example.service_a.utils.RetryConfigUtil;
import feign.Feign;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import(FeignConfig.class)
public class FeignClientConfig4ServiceB {

    @Value("${app.serviceB.url}")
    private String serviceBURL;

    private final static String clientName = "serviceBClientName";

    @Bean
    public Retry serviceBRetry() {
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

    @Bean
    @Primary
    public ServiceBClient serviceBClient(Retry serviceBRetry, SpringMvcContract springContract) {
        FeignDecorators decorators = FeignDecorators.builder()
                .withRetry(serviceBRetry)
                .build();

        return Feign.builder()
                .addCapability(Resilience4jFeign.capability(decorators))
                .contract(springContract)  // KRITISCH: Spring Contract für @GetMapping
                .target(ServiceBClient.class, serviceBURL);
    }
}