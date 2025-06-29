package com.example.service_a.adapter.http.client;

import com.example.service_a.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "serviceC", configuration = FeignConfig.class)
public interface ServiceCClient {
    @GetMapping("/api/resource")
    String getResource();
}