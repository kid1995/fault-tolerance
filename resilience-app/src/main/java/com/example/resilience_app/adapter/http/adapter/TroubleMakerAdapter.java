package com.example.resilience_app.adapter.http.adapter;

import com.example.resilience_app.adapter.http.client.ProgrammaticRetryClient;
import com.example.resilience_app.adapter.http.client.QualifierRetryClient;
import com.example.resilience_app.adapter.http.client.AnnotationRetryService;
import com.example.resilience_app.model.ErrorTestRequest;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TroubleMakerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TroubleMakerAdapter.class);

    private final ProgrammaticRetryClient programmaticRetryClient;
    private final QualifierRetryClient qualifierRetryClient;
    private final AnnotationRetryService annotationRetryService;

    public TroubleMakerAdapter(
            ProgrammaticRetryClient programmaticRetryClient,
            QualifierRetryClient qualifierRetryClient,
            AnnotationRetryService annotationRetryService) {
        this.programmaticRetryClient = programmaticRetryClient;
        this.qualifierRetryClient = qualifierRetryClient;
        this.annotationRetryService = annotationRetryService;
    }

    /**
     * Call service with PROGRAMMATIC retry configuration
     * Uses Feign builder with custom RetryConfig (Database-friendly strategy)
     */
    public String getResourceFromServiceB(ErrorTestRequest errorRequest) {
        logger.info("🔧 [PROGRAMMATIC-RETRY] Starting call with FEIGN BUILDER + RetryConfig");
        logger.info("🔧 [PROGRAMMATIC-RETRY] Strategy: Database-friendly (4 attempts, exponential random backoff)");
        logger.info("🔧 [PROGRAMMATIC-RETRY] Error config: errorCode={}, errorRate={}, delay={}ms",
                errorRequest.getErrorCode(), errorRequest.getErrorRate(), errorRequest.getResponseDelayMs());

        long startTime = System.currentTimeMillis();
        try {
            String result = programmaticRetryClient.simulateError(errorRequest.getErrorCode(), errorRequest);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("✅ [PROGRAMMATIC-RETRY] Call SUCCESSFUL after {}ms: {}", duration, result);
            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [PROGRAMMATIC-RETRY] Call FAILED after {}ms: {} - {}",
                    duration, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Call service with @QUALIFIER retry configuration
     * Uses @Qualifier annotation with YAML configuration
     */
    public String getResourceFromServiceC(ErrorTestRequest errorRequest) {
        logger.info("🏷️ [QUALIFIER-RETRY] Starting call with @QUALIFIER + YAML configuration");
        logger.info("🏷️ [QUALIFIER-RETRY] Strategy: qualifierRetryConfig from application.yml");
        logger.info("🏷️ [QUALIFIER-RETRY] Error config: errorCode={}, errorRate={}, delay={}ms",
                errorRequest.getErrorCode(), errorRequest.getErrorRate(), errorRequest.getResponseDelayMs());

        long startTime = System.currentTimeMillis();
        try {
            String result = qualifierRetryClient.simulateError(errorRequest.getErrorCode(), errorRequest);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("✅ [QUALIFIER-RETRY] Call SUCCESSFUL after {}ms: {}", duration, result);
            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [QUALIFIER-RETRY] Call FAILED after {}ms: {} - {}",
                    duration, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Call service with @RETRY ANNOTATION configuration
     * Uses @Retry annotation with YAML configuration
     */
    @Retry(name = "annotationRetryConfig")
    public String callTroubleMakerWithError(ErrorTestRequest errorRequest) {
        logger.info("📝 [ANNOTATION-RETRY] Starting call with @RETRY ANNOTATION + YAML configuration");
        logger.info("📝 [ANNOTATION-RETRY] Strategy: annotationRetryConfig from application.yml");
        logger.info("📝 [ANNOTATION-RETRY] Error config: errorCode={}, errorRate={}, delay={}ms",
                errorRequest.getErrorCode(), errorRequest.getErrorRate(), errorRequest.getResponseDelayMs());

        long startTime = System.currentTimeMillis();
        try {
            String result = annotationRetryService.simulateError(errorRequest.getErrorCode(), errorRequest);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("✅ [ANNOTATION-RETRY] Call SUCCESSFUL after {}ms: {}", duration, result);
            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("🔔 [ANNOTATION-RETRY] Retry attempt FAILED after {}ms: {} - {}",
                    duration, e.getClass().getSimpleName(), e.getMessage());
            // Re-throw to trigger next retry attempt
            throw e;
        }
    }
}