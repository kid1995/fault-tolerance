package com.example.service_a.adapter.http.adapter;

import com.example.service_a.adapter.http.client.ServiceBClient;
import com.example.service_a.adapter.http.client.ServiceCClient;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

@Component
public class ExternServiceAdapter {

     private final ServiceBClient serviceBClient;
     private final ServiceCClient serviceCClient;

     public ExternServiceAdapter(ServiceBClient serviceBClient, ServiceCClient serviceCClient) {
         this.serviceBClient = serviceBClient;
         this.serviceCClient = serviceCClient;
     }


        public String getResourceFromServiceB() {
            return serviceBClient.getResource();
        }

        public String getResourceFromServiceC() {
            return serviceCClient.getResource();
        }
}
