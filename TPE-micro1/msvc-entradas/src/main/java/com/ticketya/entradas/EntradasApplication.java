package com.ticketya.entradas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.cloud.openfeign.EnableFeignClients
@SpringBootApplication
public class EntradasApplication {

	public static void main(String[] args) {
		SpringApplication.run(EntradasApplication.class, args);
	}

}
