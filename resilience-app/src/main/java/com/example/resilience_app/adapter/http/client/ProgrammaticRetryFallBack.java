package com.example.resilience_app.adapter.http.client;

import com.example.resilience_app.model.ErrorTestRequest;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgrammaticRetryFallBack implements ProgrammaticRetryClient {
    private static final Logger logger = LoggerFactory.getLogger(ProgrammaticRetryFallBack.class);

    private Exception e;

    public ProgrammaticRetryFallBack(Exception cause) {
        this.e = cause;
    }

    public String simulateError(String errorCode, ErrorTestRequest errorConfig) {
        if (e == null) {
            logger.error("🔙 [PROGRAMMATIC-RETRY-FALLBACK] Call FAILED with unknown exception");
            return "Unknown exception";
        } else {
            logger.error("🔙 [PROGRAMMATIC-RETRY-FALLBACK] Call FAILED with exception: {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());

            if (e instanceof FeignException) {
                return "Feign Exception";
            } else {
                return "Other exception";
            }
        }



    }
}
