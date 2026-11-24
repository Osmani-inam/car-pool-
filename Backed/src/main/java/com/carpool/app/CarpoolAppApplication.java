package com.carpool.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarpoolAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarpoolAppApplication.class, args);
    }
}
