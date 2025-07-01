package com.example.resilience_app.config.service;

import com.example.resilience_app.adapter.http.client.ProgrammaticRetryClient;
import com.example.resilience_app.utils.RetryConfigUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.codec.Decoder;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
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

    @Value("${app.troubleMaker.url}")
    private String troubleMakerURL;

    private final static String clientName = "programmaticRetryClient";

    @Bean
    public Retry programmaticRetry() {
        RetryConfig retryConfig = RetryConfigUtil.createRandomBackoffRetry();
        return Retry.of(clientName, retryConfig);
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