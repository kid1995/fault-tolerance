package com.example.trouble_maker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorConfig {

    private double errorRate; // Error rate (0.0 - 1.0). 0.0 = no errors, 1.0 = always an error.
    private int responseDelayMs; // Simulated server response delay (ms).
    private int retryAfterSeconds; // Simulated "Retry-After" header value (seconds).
    private long timeoutDelayMs; // Timeout delay for 504/408 errors (ms).
    private boolean enabled; // Enables/disables error simulation.

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
        this.errorRate = Math.clamp(errorRate, 0.0, 1.0);
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