package com.example.service_a.adapter.http.client;

import com.example.service_a.config.service.ProgrammaticRetryConfig;
import com.example.service_a.model.ErrorTestRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "troubleMakerClientWithQualifiers",
        url = "${app.troubleMaker.url}",
        configuration = ProgrammaticRetryConfig.class // This uses the programmatic configuration class
)
public interface ProgrammaticRetryClient {
    @GetMapping("/api/errors")
    String simulateError(@RequestParam("errorName") String errorName, @RequestBody ErrorTestRequest errorConfig);
}