package com.example.resilience_app.adapter.message;

import com.example.resilience_app.adapter.http.adapter.TroubleMakerAdapter;
import com.example.resilience_app.model.ErrorTestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Component
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private final TroubleMakerAdapter troubleMakerAdapter;

    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    public MessageConsumer(TroubleMakerAdapter troubleMakerAdapter) {
        this.troubleMakerAdapter = troubleMakerAdapter;
    }

    @Bean
    public Consumer<ErrorTestRequest> onErrorTestRequest() {
        return event -> {
            long startTime = System.currentTimeMillis();
            int messageNumber = processedCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();

            logger.info("🔄 [KAFKA-MESSAGE-{}] Processing: {}",
                    messageNumber, event.getDescription());

            try {
                // Sequential processing - each message block until completion
                String result = troubleMakerAdapter.simulateErrorWithProgrammaticRetry(event);

                long duration = System.currentTimeMillis() - startTime;
                int successTotal = successCount.incrementAndGet();

                logger.info("✅ [KAFKA-MESSAGE-{}] SUCCESS with result {}  after {}ms | Total success: {}",
                        messageNumber, result, duration, successTotal);

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                int errorTotal = errorCount.incrementAndGet();

                logger.error("❌ [KAFKA-MESSAGE-{}] FAILED after {}ms | Total errors: {} | Exception: {}",
                        messageNumber, duration, errorTotal, e.getMessage());
            }
        };
    }
}