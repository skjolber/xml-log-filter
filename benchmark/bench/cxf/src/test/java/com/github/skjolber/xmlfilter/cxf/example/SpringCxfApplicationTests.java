package com.github.skjolber.xmlfilter.cxf.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;

import com.github.skjolber.xmlns.schema.logger.LogHeader;
import com.github.skjolber.xmlns.schema.logger.LoggerException;
import com.github.skjolber.xmlns.schema.logger.LoggerPort;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageRequest;

@SpringBootTest(classes = SimpleBootCxfApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration
@DirtiesContext
public class SpringCxfApplicationTests {

    @Autowired
    @Qualifier("loggerPortClientProxyBean")
    private LoggerPort client;

    @Test
    public void test1() throws LoggerException {
    	PerformLogMessageRequest request = new PerformLogMessageRequest();
    	LogHeader header = new LogHeader();
    	header.setSessionId("sessionId");
    	header.setUserId("userId");
        client.performLogMessage(request, header);
    }
    
}