package com.example.resilience_app.adapter.http.adapter;

import com.example.resilience_app.adapter.http.client.ProgrammaticRetryClient;
import com.example.resilience_app.adapter.http.client.AnnotationRetryService;
import com.example.resilience_app.model.ErrorTestRequest;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class TroubleMakerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TroubleMakerAdapter.class);

    private final ProgrammaticRetryClient programmaticRetryClient;
    private final AnnotationRetryService annotationRetryService;

    public TroubleMakerAdapter(
            ProgrammaticRetryClient programmaticRetryClient,
            AnnotationRetryService annotationRetryService) {
        this.programmaticRetryClient = programmaticRetryClient;
        this.annotationRetryService = annotationRetryService;
    }

    /**
     * Call service with PROGRAMMATIC retry configuration
     */
    public String simulateErrorWithProgrammaticRetry(ErrorTestRequest errorRequest) {
        long startTime = System.currentTimeMillis();
        String result = programmaticRetryClient.simulateError(errorRequest.getErrorCode(), errorRequest);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("🏁 [PROGRAMMATIC-RETRY] call end after {}ms: {}", duration, result);
        return result;
    }

    /**
     * Call service with @RETRY ANNOTATION configuration
     * Original method signature: simulateErrorWithAnnotationRetry(ErrorTestRequest errorRequest)
     */
    @Retry(name = "annotationRetryConfig", fallbackMethod = "simulateErrorWithAnnotationRetryFallback")
    public String simulateErrorWithAnnotationRetry(ErrorTestRequest errorRequest) {
        long startTime = System.currentTimeMillis();
        String result = annotationRetryService.simulateError(errorRequest.getErrorCode(), errorRequest);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("🏁 [ANNOTATION-RETRY] call end after {}ms: {}", duration, result);
        return result;
    }

    /**
     * CRITICAL: The error message shows Resilience4j expects THIS exact signature:
     * simulateErrorWithAnnotationRetryFallback(ErrorTestRequest, Throwable)
     *
     * NOT: simulateErrorWithAnnotationRetryFallback(Throwable, ErrorTestRequest)
     */
    public String simulateErrorWithAnnotationRetryFallback(ErrorTestRequest errorRequest, Throwable throwable) {
        logger.warn("🔙 [ANNOTATION-RETRY-FALLBACK] All retries exhausted, executing fallback");
        logger.warn("🔙 [ANNOTATION-RETRY-FALLBACK] Original error: {} - {}",
                throwable.getClass().getSimpleName(), throwable.getMessage());

        switch ( throwable.getClass().getSimpleName() ) {
            case "ConnectException":
                logger.warn("🔙 [ANNOTATION-RETRY-FALLBACK] Connection error detected");
                break;
            case "RetryableException":
                logger.warn("🔙 [ANNOTATION-RETRY-FALLBACK] Feign retry exhausted");
                break;
            default:
                logger.warn("🔙 [ANNOTATION-RETRY-FALLBACK] General error fallback");
        }
        //TODO: return a meaningful error response, this is just an example
        return String.format(
                "{\"status\":\"FALLBACK\",\"message\":\"Service temporarily unavailable after annotation retry\",\"errorCode\":\"%s\",\"originalError\":\"%s\",\"timestamp\":\"%s\"}",
                errorRequest.getErrorCode(),
                throwable.getMessage(),
                LocalDateTime.now()
        );
    }


}