package com.example.resilience_app.adapter.message;

import com.example.resilience_app.adapter.http.adapter.TroubleMakerAdapter;
import com.example.resilience_app.model.ErrorTestRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class MessageConsumer {
    private final TroubleMakerAdapter troubleMakerAdapter;

    public MessageConsumer(TroubleMakerAdapter troubleMakerAdapter) {
        this.troubleMakerAdapter = troubleMakerAdapter;
    }
    @Bean
    public Consumer<ErrorTestRequest> onErrorTestRequest() {
        return event -> {
            System.out.println("Consuming " + event + " onApplicationReview");
            this.troubleMakerAdapter.simulateErrorWithProgrammaticRetry(event);
        };

    }
}
