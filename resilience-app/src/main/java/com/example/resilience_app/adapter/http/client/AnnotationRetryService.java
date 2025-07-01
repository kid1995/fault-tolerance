package com.example.resilience_app.adapter.http.client;

import com.example.resilience_app.model.ErrorTestRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "annotationRetryService", url = "${app.troubleMaker.url}")
public interface AnnotationRetryService {
    @PostMapping("/api/errors")
    String simulateError(@RequestParam("errorCode") String errorCode, @RequestBody ErrorTestRequest errorConfig);
}