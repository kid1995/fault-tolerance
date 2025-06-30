package com.example.service_a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse {

    private String status;
    private String message;
    private String data;
    private LocalDateTime timestamp;
    private String errorType;

    // Default constructor
    public ServiceResponse() {}

    // Constructor
    public ServiceResponse(String status, String message, String data, LocalDateTime timestamp, String errorType) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.errorType = errorType;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return "ServiceResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                ", timestamp=" + timestamp +
                ", errorType='" + errorType + '\'' +
                '}';
    }
}