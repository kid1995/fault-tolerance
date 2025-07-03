package com.example.resilience_app.adapter.message;

import com.example.resilience_app.model.ErrorTestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MessageProducer {
    private final StreamBridge streamBridge;

    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Value("${kafka-custom-config.topics.sequential-consumer.name}")
    private String sequentialConsumerTopicName;

    @Value("${kafka-custom-config.topics.concurrent-consumer.name}")
    private String concurrentConsumerTopicName;

    public MessageProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendSequentialMessages(
            int numberOfMessages,
            ErrorTestRequest errorRequest) {
        sendMessages(sequentialConsumerTopicName, numberOfMessages, errorRequest, "SEQUENTIAL");
    }

    public void sendConcurrentMessages(
            int numberOfMessages,
            ErrorTestRequest errorRequest) {
        sendMessages(concurrentConsumerTopicName, numberOfMessages, errorRequest, "CONCURRENT");
    }


    private void sendMessage(String bindingName, ErrorTestRequest message) {
        boolean sent = streamBridge.send(bindingName, message);
        if (!sent) {
            throw new RuntimeException("Failed to send message to binding: " + bindingName);
        }
    }


    private void sendMessages(
            String bindingName,
            int numberOfMessages,
            ErrorTestRequest errorRequest,
            String strategyName) {
        logger.info("📦 [{}] Processing {} messages", strategyName, numberOfMessages);

        String currentMessageIndex = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        for (int i = 0; i < numberOfMessages; i++) {
            ErrorTestRequest messageRequest = createBatchMessage(errorRequest, currentMessageIndex + '_' + i);
            sendMessage(bindingName, messageRequest);
            logger.info("📦 [{}] Sent message: {} - {}", strategyName, messageRequest.getDescription(), messageRequest);
        }

    }

    /**
     * Create a unique message for batch processing
     */
    private ErrorTestRequest createBatchMessage(ErrorTestRequest original, String messageIndex) {
        ErrorTestRequest batchMessage = new ErrorTestRequest();
        batchMessage.setErrorCode(original.getErrorCode());
        batchMessage.setErrorRate(original.getErrorRate());
        batchMessage.setResponseDelayMs(original.getResponseDelayMs());
        batchMessage.setRetryAfterSeconds(original.getRetryAfterSeconds());
        batchMessage.setTimeoutDelayMs(original.getTimeoutDelayMs());
        batchMessage.setEnabled(original.isEnabled());
        batchMessage.setDescription(original.getDescription() + " - Message #" + messageIndex);

        return batchMessage;
    }
}
