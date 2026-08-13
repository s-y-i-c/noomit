package com.noomit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NoomitBackendApplication {

    static void main(String[] args) {
        SpringApplication.run(NoomitBackendApplication.class, args);
    }
}
