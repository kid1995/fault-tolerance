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

    // Counters for sequential consumer
    private final AtomicInteger sequentialProcessedCount = new AtomicInteger(0);
    private final AtomicInteger sequentialSuccessCount = new AtomicInteger(0);
    private final AtomicInteger sequentialErrorCount = new AtomicInteger(0);

    // Counters for concurrent consumer
    private final AtomicInteger concurrentProcessedCount = new AtomicInteger(0);
    private final AtomicInteger concurrentSuccessCount = new AtomicInteger(0);
    private final AtomicInteger concurrentErrorCount = new AtomicInteger(0);

    public MessageConsumer(TroubleMakerAdapter troubleMakerAdapter) {
        this.troubleMakerAdapter = troubleMakerAdapter;
    }

    /**
     * SEQUENTIAL MESSAGE CONSUMER
     * Processes one message at a time (concurrency = 1)
     * Binding: onErrorTestRequestSequential-in-0
     */
    @Bean
    public Consumer<ErrorTestRequest> onErrorTestRequestSequential() {
        return event -> {
            long startTime = System.currentTimeMillis();
            int messageNumber = sequentialProcessedCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();

            logger.info("🔄 [SEQUENTIAL-{}] Thread: {} | Processing: {}",
                    messageNumber, threadName, event.getDescription());

            try {
                // Sequential processing - each message blocks until completion
                String result = troubleMakerAdapter.simulateErrorWithProgrammaticRetry(event);

                long duration = System.currentTimeMillis() - startTime;
                int successTotal = sequentialSuccessCount.incrementAndGet();

                logger.info("✅ [SEQUENTIAL-{}] SUCCESS after {}ms | Thread: {} | Total success: {}",
                        messageNumber, duration, threadName, successTotal);

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                int errorTotal = sequentialErrorCount.incrementAndGet();

                logger.error("❌ [SEQUENTIAL-{}] FAILED after {}ms | Thread: {} | Total errors: {} | Exception: {}",
                        messageNumber, duration, threadName, errorTotal, e.getMessage());
            }
        };
    }

    /**
     * CONCURRENT MESSAGE CONSUMER
     * Processes multiple messages simultaneously (concurrency = 3)
     * Binding: onErrorTestRequestConcurrent-in-0
     */
    @Bean
    public Consumer<ErrorTestRequest> onErrorTestRequestConcurrent() {
        return event -> {
            long startTime = System.currentTimeMillis();
            int messageNumber = concurrentProcessedCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            logger.info("🔄 [CONCURRENT-{}] Thread: {} | Processing: {}",
                    messageNumber, threadName, event.getDescription());
            try {

                // Concurrent processing - multiple messages can be processed simultaneously
                String result = troubleMakerAdapter.simulateErrorWithProgrammaticRetry(event);

                long duration = System.currentTimeMillis() - startTime;
                int successTotal = concurrentSuccessCount.incrementAndGet();

                logger.info("✅ [CONCURRENT-{}] SUCCESS after {}ms | Thread: {} | Total success: {}",
                        messageNumber, duration, threadName, successTotal);

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                int errorTotal = concurrentErrorCount.incrementAndGet();

                logger.error("❌ [CONCURRENT-{}] FAILED after {}ms | Thread: {} | Total errors: {} | Exception: {}",
                        messageNumber, duration, threadName, errorTotal, e.getMessage());
            }

        };
    }
}