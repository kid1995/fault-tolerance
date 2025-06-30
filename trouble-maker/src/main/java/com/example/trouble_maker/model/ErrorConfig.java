package com.example.trouble_maker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorConfig {
    
    private double errorRate = 1.0; // Default to 100% error rate
    private int responseDelayMs = 0; // No delay by default
    private int retryAfterSeconds = 30; // Retry-After header value for 429 and 503
    private long timeoutDelayMs = 5000; // Timeout delay for 504 and 408
    private boolean enabled = true;

    // Default constructor
    public ErrorConfig() {}
    
    // Constructor with all fields
    public ErrorConfig(double errorRate, int responseDelayMs, int retryAfterSeconds,
                      long timeoutDelayMs, boolean enabled) {
        this.errorRate = errorRate;
        this.responseDelayMs = responseDelayMs;
        this.retryAfterSeconds = retryAfterSeconds;
        this.timeoutDelayMs = timeoutDelayMs;
        this.enabled = enabled;
    }

    // Getters and Setters
    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = Math.max(0.0, Math.min(1.0, errorRate)); // Clamp between 0 and 1
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

    @Override
    public String toString() {
        return "ErrorConfig{" +
                "errorRate=" + errorRate +
                ", responseDelayMs=" + responseDelayMs +
                ", retryAfterSeconds=" + retryAfterSeconds +
                ", timeoutDelayMs=" + timeoutDelayMs +
                ", enabled=" + enabled +
                '}';
    }
}