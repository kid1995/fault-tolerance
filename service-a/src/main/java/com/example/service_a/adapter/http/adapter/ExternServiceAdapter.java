package com.example.service_a.adapter.http.adapter;

import com.example.service_a.adapter.http.client.ServiceBClient;
import com.example.service_a.adapter.http.client.ServiceCClient;
import com.example.service_a.adapter.http.client.TroubleMakerClient;
import com.example.service_a.model.ErrorTestRequest;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExternServiceAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ExternServiceAdapter.class);

    private final ServiceBClient serviceBClient;
    private final ServiceCClient serviceCClient;
    private final TroubleMakerClient troubleMakerClient;

    public ExternServiceAdapter(ServiceBClient serviceBClient, ServiceCClient serviceCClient, TroubleMakerClient troubleMakerClient, ServiceCClient serviceCRetry) {
        this.serviceBClient = serviceBClient;
        this.serviceCClient = serviceCClient;
        this.troubleMakerClient = troubleMakerClient;
    }

    /**
     * Call Service B with programmatic retry configuration
     */
    public String getResourceFromServiceB(ErrorTestRequest errorRequest) {
        logger.info("Calling Service B with programmatic retry configuration");
        return serviceBClient.simulateError(errorRequest.getErrorCode(), errorRequest);

    }

    /**
     * Call Service C with qualifier annotation to use specific retry configuration
     */
    public String getResourceFromServiceC(ErrorTestRequest errorRequest) {
        logger.info("Calling Service C with @Qualifier annotation");
        return serviceCClient.simulateError(errorRequest.getErrorCode(), errorRequest);
    }


    /**
     * Call trouble-maker service to simulate an error
     */
    @Retry(name = "troubleMakerRetry")
    public String callTroubleMakerWithError(ErrorTestRequest errorRequest) {
        logger.info("Calling trouble-maker with only @Retry annotation: {}", errorRequest);
        return troubleMakerClient.simulateError(errorRequest.getErrorCode(), errorRequest);
    }

}