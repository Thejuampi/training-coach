package com.training.coach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TrainingCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingCoachApplication.class, args);
    }
}
