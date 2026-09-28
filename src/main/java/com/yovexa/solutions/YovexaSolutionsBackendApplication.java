package com.yovexa.solutions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class YovexaSolutionsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YovexaSolutionsBackendApplication.class, args);
    }
}
