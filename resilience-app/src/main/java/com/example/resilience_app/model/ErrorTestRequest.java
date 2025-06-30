package com.example.resilience_app.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorTestRequest {

    private String errorCode = "503"; // Default to 503 Service Unavailable
    private double errorRate = 1.0; // 100% error rate by default
    private int responseDelayMs = 0;
    private int retryAfterSeconds = 30;
    private long timeoutDelayMs = 5000;
    private boolean enabled = true;
    private String description;

    // Default constructor
    public ErrorTestRequest() {}

    // Constructor
    public ErrorTestRequest(String errorCode, double errorRate, int responseDelayMs,
                            int retryAfterSeconds, long timeoutDelayMs, boolean enabled, String description) {
        this.errorCode = errorCode;
        this.errorRate = errorRate;
        this.responseDelayMs = responseDelayMs;
        this.retryAfterSeconds = retryAfterSeconds;
        this.timeoutDelayMs = timeoutDelayMs;
        this.enabled = enabled;
        this.description = description;
    }

    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = Math.max(0.0, Math.min(1.0, errorRate));
    }

    public int getResponseDelayMs() {
        return responseDelayMs;
    }

    public void setResponseDelayMs(int responseDelayMs) {
        this.responseDelayMs = Math.max(0, responseDelayMs);
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public void setRetryAfterSeconds(int retryAfterSeconds) {
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long getTimeoutDelayMs() {
        return timeoutDelayMs;
    }

    public void setTimeoutDelayMs(long timeoutDelayMs) {
        this.timeoutDelayMs = Math.max(0, timeoutDelayMs);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ErrorTestRequest{" +
                "errorCode='" + errorCode + '\'' +
                ", errorRate=" + errorRate +
                ", responseDelayMs=" + responseDelayMs +
                ", retryAfterSeconds=" + retryAfterSeconds +
                ", timeoutDelayMs=" + timeoutDelayMs +
                ", enabled=" + enabled +
                ", description='" + description + '\'' +
                '}';
    }
}