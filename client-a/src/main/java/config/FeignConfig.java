package config;


import com.example.client_a.adapter.http.client.ServiceBClient;
import feign.Feign;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class FeignConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .build();

        RetryRegistry registry = RetryRegistry.of(retryConfig);
        registry.retry("serviceBRetry", retryConfig);
        return registry;
    }

    @Bean
    public ServiceBClient serviceBClient(RetryRegistry retryRegistry) {

        // 1) Retry-Instanz aus dem Registry holen
        Retry retry = retryRegistry.retry("serviceBRetry");

        // 2) Decorators mit Retry-Policy bauen
        FeignDecorators decorators = FeignDecorators.builder()
                .withRetry(retry)
                .build();

        // 3) ServiceBClient direkt erstellen
        return Feign.builder()
                .addCapability(Resilience4jFeign.capability(decorators))
                .target(ServiceBClient.class, "http://localhost:8081");
    }
}