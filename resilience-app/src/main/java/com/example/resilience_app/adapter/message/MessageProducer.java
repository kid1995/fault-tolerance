package com.example.resilience_app.adapter.message;

import com.example.resilience_app.model.ErrorTestRequest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {
    private final StreamBridge streamBridge;

    public MessageProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendMessage(String bindingName, ErrorTestRequest message) {
        boolean sent = streamBridge.send(bindingName, message);
        if (!sent) {
            throw new RuntimeException("Failed to send message to binding: " + bindingName);
        }
    }

    public void sendErrorTestRequest(ErrorTestRequest errorTestRequest) {
        String bindingName = "errorTestRequest-out-0"; // Adjust the binding name as needed
        sendMessage(bindingName, errorTestRequest);
    }
}
