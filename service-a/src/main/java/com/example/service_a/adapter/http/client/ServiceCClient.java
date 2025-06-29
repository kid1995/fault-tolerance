package com.example.service_a.adapter.http.client;

import com.example.service_a.config.service.FeignClientConfig4ServiceC;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "serviceC",
        configuration = FeignClientConfig4ServiceC.class,
        qualifiers = "serviceCRetry"  // This binds to the retry instance
)
public interface ServiceCClient {
    @GetMapping("/api/resource")
    String getResource();
}