package com.example.resilience_app.adapter.message;

import com.example.resilience_app.model.ErrorTestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MessageProducer {
    private final StreamBridge streamBridge;
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    // ✅ Use Spring Cloud Stream binding names instead of topic names
    private static final String SEQUENTIAL_BINDING = "resilience-lab-sequential-out-0";
    private static final String CONCURRENT_BINDING = "resilience-lab-concurrent-out-0";

    public MessageProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendSequentialMessages(int numberOfMessages, ErrorTestRequest errorRequest) {
        logger.info("📦 [SEQUENTIAL-PRODUCER] Starting to send {} messages", numberOfMessages);
        sendMessages(SEQUENTIAL_BINDING, numberOfMessages, errorRequest, "SEQUENTIAL");
        logger.info("✅ [SEQUENTIAL-PRODUCER] Finished sending {} messages", numberOfMessages);
    }

    public void sendConcurrentMessages(int numberOfMessages, ErrorTestRequest errorRequest) {
        logger.info("📦 [CONCURRENT-PRODUCER] Starting to send {} messages", numberOfMessages);
        sendMessages(CONCURRENT_BINDING, numberOfMessages, errorRequest, "CONCURRENT");
        logger.info("✅ [CONCURRENT-PRODUCER] Finished sending {} messages", numberOfMessages);
    }

    private void sendMessages(String bindingName, int numberOfMessages, ErrorTestRequest errorRequest, String strategyName) {
        logger.info("📦 [{}] Processing {} messages to binding: {}", strategyName, numberOfMessages, bindingName);

        String batchId = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        for (int i = 1; i <= numberOfMessages; i++) {
            ErrorTestRequest messageRequest = createBatchMessage(errorRequest, batchId + "_" + i);

            try {
                boolean sent = streamBridge.send(bindingName, messageRequest);
                if (sent) {
                    logger.info("📤 [{}] Message {}/{} sent successfully: {}",
                            strategyName, i, numberOfMessages, messageRequest.getDescription());
                } else {
                    logger.error("❌ [{}] Failed to send message {}/{}: {}",
                            strategyName, i, numberOfMessages, messageRequest.getDescription());
                }
            } catch (Exception e) {
                logger.error("❌ [{}] Exception sending message {}/{}: {}",
                        strategyName, i, numberOfMessages, e.getMessage(), e);
            }
        }
    }

    private ErrorTestRequest createBatchMessage(ErrorTestRequest original, String messageIndex) {
        ErrorTestRequest batchMessage = new ErrorTestRequest();
        batchMessage.setErrorCode(original.getErrorCode());
        batchMessage.setErrorRate(original.getErrorRate());
        batchMessage.setResponseDelayMs(original.getResponseDelayMs());
        batchMessage.setRetryAfterSeconds(original.getRetryAfterSeconds());
        batchMessage.setTimeoutDelayMs(original.getTimeoutDelayMs());
        batchMessage.setEnabled(original.isEnabled());
        batchMessage.setDescription(original.getDescription() + " - Msg#" + messageIndex);
        return batchMessage;
    }
}