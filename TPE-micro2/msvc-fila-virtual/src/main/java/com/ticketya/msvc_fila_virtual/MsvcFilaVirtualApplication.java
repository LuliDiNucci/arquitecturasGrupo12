package com.ticketya.msvc_fila_virtual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class MsvcFilaVirtualApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcFilaVirtualApplication.class, args);
	}

}
