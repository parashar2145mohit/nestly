package com.nestly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NestlyApplication {
    public static void main(String[] args) { SpringApplication.run(NestlyApplication.class, args); }
}

