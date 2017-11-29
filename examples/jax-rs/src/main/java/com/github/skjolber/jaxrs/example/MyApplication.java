package com.github.skjolber.jaxrs.example;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.github.skjolber.jaxrs.example"})
public class MyApplication extends SpringBootServletInitializer {
	
    public static void main(String[] args) {
    	SpringApplication.run(MyApplication.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	return application.sources(new SpringApplicationBuilder(JersyConfiguration.class));
    }
}