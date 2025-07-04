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
    private static final String RESILIENCE_LAB_OUT = "resilience-lab-out-0";


    public MessageProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendSequentialMessages(int numberOfMessages, ErrorTestRequest errorRequest) {
        logger.info("📦 [PRODUCER] Starting to send {} messages", numberOfMessages);
        sendMessages(numberOfMessages, errorRequest);
        logger.info("✅ [PRODUCER] Finished sending {} messages", numberOfMessages);
    }


    private void sendMessages(int numberOfMessages, ErrorTestRequest errorRequest) {
        logger.info("📦 [PRODUCER] Processing {} messages to binding: {}", numberOfMessages, MessageProducer.RESILIENCE_LAB_OUT);

        String batchId = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        for (int i = 1; i <= numberOfMessages; i++) {
            ErrorTestRequest messageRequest = createBatchMessage(errorRequest, batchId + "_" + i);

            try {
                boolean sent = streamBridge.send(MessageProducer.RESILIENCE_LAB_OUT, messageRequest);
                if (sent) {
                    logger.info("📤 [PRODUCER] Message {}/{} sent successfully: {}",
                            i, numberOfMessages, messageRequest.getDescription());
                } else {
                    logger.error("❌ [PRODUCER] Failed to send message {}/{}: {}",
                            i, numberOfMessages, messageRequest.getDescription());
                }
            } catch (Exception e) {
                logger.error("❌ [PRODUCER] Exception sending message {}/{}: {}",
                        i, numberOfMessages, e.getMessage(), e);
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