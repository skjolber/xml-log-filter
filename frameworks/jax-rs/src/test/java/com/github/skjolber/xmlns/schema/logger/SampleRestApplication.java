package com.github.skjolber.xmlns.schema.logger;


import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogContainerFilter;
import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Jersey JAXB example application.
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
public class SampleRestApplication {

    private static final URI BASE_URI = URI.create("http://localhost:8080/logger/");

    @Autowired
    private Bus bus;
    
    @Bean
    public Server rsServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(bus);
        endpoint.setAddress("/");
        // Register 2 JAX-RS root resources supporting "/sayHello/{id}" and "/sayHello2/{id}" relative paths
        endpoint.setServiceBeans(Arrays.<Object>asList(new LoggerResource1(), new LoggerResource2()));
        endpoint.setProviders(Arrays.asList(XmlLogContainerFilter.class));
        return endpoint.create();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(SampleRestApplication.class, args);
    }    
}