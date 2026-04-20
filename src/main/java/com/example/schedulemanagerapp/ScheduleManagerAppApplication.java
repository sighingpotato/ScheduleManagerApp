package com.example.schedulemanagerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ScheduleManagerAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleManagerAppApplication.class, args);
    }

}
