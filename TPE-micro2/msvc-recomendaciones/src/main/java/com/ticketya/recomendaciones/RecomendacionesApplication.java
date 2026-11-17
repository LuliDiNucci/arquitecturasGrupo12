
package com.ticketya.recomendaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RecomendacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecomendacionesApplication.class, args);
    }
}
