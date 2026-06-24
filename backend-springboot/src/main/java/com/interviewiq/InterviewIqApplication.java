package com.interviewiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class InterviewIqApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterviewIqApplication.class, args);
    }
}
