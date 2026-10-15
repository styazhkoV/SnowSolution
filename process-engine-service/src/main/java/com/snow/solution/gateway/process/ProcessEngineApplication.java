package com.snow.solution.gateway.process;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProcessEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessEngineApplication.class, args);
    }
}