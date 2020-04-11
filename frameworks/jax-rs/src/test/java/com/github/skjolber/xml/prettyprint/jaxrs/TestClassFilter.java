package com.github.skjolber.xml.prettyprint.jaxrs;

import static javax.ws.rs.client.Entity.xml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.ws.rs.core.GenericEntity;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

import com.github.skjolber.xmlns.schema.logger.PerformLogMessageRequest;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageResponse;
import com.github.skjolber.xmlns.schema.logger.SampleRestApplication;

@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestClassFilter {
	
	@LocalServerPort
	private int port;
	
    @Test
    public void testPerformLogMessageObjects() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");
        
        WebClient wc = WebClient.create("http://localhost:" + port + "/services/logger2");        
        wc.accept("application/xml");
        
        PerformLogMessageResponse performLogMessage = wc.path("performLogMessageObject").post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }

}