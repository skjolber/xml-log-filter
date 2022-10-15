package com.github.skjolber.xmlfilter.cxf.example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;

import com.github.skjolber.xmlns.schema.logger.LogHeader;
import com.github.skjolber.xmlns.schema.logger.LoggerPort;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageRequest;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageResponse;

import org.junit.jupiter.api.Test;

@SpringBootTest(classes = SimpleBootCxfApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration
@DirtiesContext
public class SpringCxfApplicationTests {

    @Autowired
    @Qualifier("loggerPortClientProxyBean")
    private LoggerPort client;

    @Test
    public void test1() throws Exception {
    	byte[] image = IOUtils.toByteArray(getClass().getResourceAsStream("/images/holygrail.jpg"));

    	PerformLogMessageRequest request = new PerformLogMessageRequest();
    	request.setImage(Base64.getEncoder().encodeToString(image));
    	
    	LogHeader header = new LogHeader();
    	header.setSessionId("sessionId");
    	header.setUserId("userId");
    
        PerformLogMessageResponse response = client.performLogMessage(request, header);
        assertNotNull(response);
    }
    
}