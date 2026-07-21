package com.fiapx.video;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot's instantiation target, not a utility class
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
