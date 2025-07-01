package com.example.resilience_app.adapter.http.client;

import com.example.resilience_app.model.ErrorTestRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for programmatic retry testing.
 * This client is used to simulate errors and test the retry mechanism.
 */
public interface ProgrammaticRetryClient {
    @PostMapping("/api/errors")
    String simulateError(@RequestParam("errorCode") String errorCode, @RequestBody ErrorTestRequest errorConfig);
}
