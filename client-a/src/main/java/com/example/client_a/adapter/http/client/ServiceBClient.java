package com.example.client_a.adapter.http.client;

import config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "serviceB", configuration = FeignConfig.class)
public interface ServiceBClient {
    @GetMapping("/api/resource")
    String getResource();
}