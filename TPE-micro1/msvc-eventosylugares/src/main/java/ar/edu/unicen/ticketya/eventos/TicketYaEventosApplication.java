package ar.edu.unicen.ticketya.eventos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "ar.edu.unicen.ticketya.eventos.feign")
public class TicketYaEventosApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketYaEventosApplication.class, args);
    }
}
