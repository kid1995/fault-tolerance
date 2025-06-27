package com.example.client_a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ClientAApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientAApplication.class, args);
	}

}
