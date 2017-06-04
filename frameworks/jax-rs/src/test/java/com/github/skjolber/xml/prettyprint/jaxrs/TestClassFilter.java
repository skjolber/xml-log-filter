package com.github.skjolber.xml.prettyprint.jaxrs;

import static javax.ws.rs.client.Entity.xml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.GenericEntity;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.github.skjolber.xmlns.schema.logger.App;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageRequest;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageResponse;

public class TestClassFilter extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
//        enable(TestProperties.LOG_TRAFFIC);
//        enable(TestProperties.DUMP_ENTITY);

        return App.createApp2();
    }

    @Test
    public void testPerformLogMessageObjects() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");
        
        PerformLogMessageResponse performLogMessage = target().path("logger2/performLogMessageObject").request("application/xml").post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }

}