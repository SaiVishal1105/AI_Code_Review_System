package com.codereview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiCodeReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeReviewApplication.class, args);
        System.out.println("""
                
                ╔══════════════════════════════════════════╗
                ║      AI Code Review System Started       ║
                ║      Backend: http://localhost:8080      ║
                ║      H2 Console: /h2-console             ║
                ╚══════════════════════════════════════════╝
                """);
    }
}
