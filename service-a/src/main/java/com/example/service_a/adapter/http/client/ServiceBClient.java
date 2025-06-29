package com.example.service_a.adapter.http.client;

import com.example.service_a.config.service.FeignClientConfig4ServiceB;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "serviceB", url = "${app.serviceB.url}", configuration = FeignClientConfig4ServiceB.class)
public interface ServiceBClient {
    @GetMapping("/api/resource")
    String getResource();
}