package com.example.client_a.adapter.http.client;

import com.example.client_a.config.service.FeignConfig4ServiceB;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "serviceB", configuration = FeignConfig4ServiceB.class)
public interface ServiceBClient {
    @GetMapping("/api/resource")
    String getResource();
}