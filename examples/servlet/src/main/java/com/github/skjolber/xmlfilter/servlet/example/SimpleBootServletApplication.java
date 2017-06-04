package com.github.skjolber.xmlfilter.servlet.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.github.skjolber")
public class SimpleBootServletApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleBootServletApplication.class, args);
    }
}
