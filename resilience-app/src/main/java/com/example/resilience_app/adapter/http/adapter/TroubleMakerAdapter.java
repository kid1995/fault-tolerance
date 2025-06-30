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
     * Call Service B with programmatic retry configuration
     */
    public String getResourceFromServiceB(ErrorTestRequest errorRequest) {
        logger.info("Calling Service B with programmatic retry configuration");
        return programmaticRetryClient.simulateError(errorRequest.getErrorCode(), errorRequest);

    }

    /**
     * Call Service C with qualifier annotation to use specific retry configuration
     */
    public String getResourceFromServiceC(ErrorTestRequest errorRequest) {
        logger.info("Calling Service C with @Qualifier annotation");
        return qualifierRetryClient.simulateError(errorRequest.getErrorCode(), errorRequest);
    }


    /**
     * Call trouble-maker service to simulate an error
     */
    @Retry(name = "annotationRetryConfig")
    public String callTroubleMakerWithError(ErrorTestRequest errorRequest) {
        logger.info("Calling trouble-maker with only @Retry annotation: {}", errorRequest);
        return annotationRetryService.simulateError(errorRequest.getErrorCode(), errorRequest);
    }

}