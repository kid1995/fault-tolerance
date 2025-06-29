package com.example.service_a.adapter.http.client;

import org.springframework.web.bind.annotation.GetMapping;

public interface ServiceBClient {
    @GetMapping("/api/resource")
    String getResource();
}