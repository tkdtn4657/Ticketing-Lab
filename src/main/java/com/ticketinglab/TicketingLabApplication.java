package com.ticketinglab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TicketingLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingLabApplication.class, args);
    }

}
