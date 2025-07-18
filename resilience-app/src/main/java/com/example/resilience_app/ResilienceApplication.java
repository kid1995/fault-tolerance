package com.example.resilience_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ResilienceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResilienceApplication.class, args);
	}

}
