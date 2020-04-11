/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.skjolber.xmlfilter.cxf;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.github.skjolber.xmlfilter.cxf.LoggingFeature;

public class SOAPLoggingTest {

    private static final String SERVICE_URI = "http://localhost:5678/test";

    @WebService(endpointInterface = "com.github.skjolber.xmlfilter.cxf.TestService")
    public final class TestServiceImplementation implements TestService {
        @Override
        public String echo(String msg) {
            return msg;
        }
    }

    @Test
    public void testSlf4j() throws MalformedURLException {
        TestService serviceImpl = new TestServiceImplementation();
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setIndent(true);
        // Setting the limit should omit parts of the body but the result should still be well formed xml
        loggingFeature.setDiscThreshold(140);
        Endpoint ep = Endpoint.publish(SERVICE_URI, serviceImpl, loggingFeature);
        TestService client = createTestClient(loggingFeature);
        client.echo("test");
        ep.stop();
    }
    
    @Test
    public void testEvents() throws MalformedURLException {
        TestService serviceImpl = new TestServiceImplementation();
        TestEventSender sender = new TestEventSender();
        LoggingFeature loggingFeature = new LoggingFeature(sender);
        
        Endpoint ep = Endpoint.publish(SERVICE_URI, serviceImpl, loggingFeature);
        TestService client = createTestClient(loggingFeature);
        client.echo("test");
        ep.stop();

        List<LogEvent> events = sender.getEvents();
        assertEquals(4, events.size());
        checkRequestOut(events.get(0));
        checkRequestIn(events.get(1));
        checkResponseOut(events.get(2));
        checkResponseIn(events.get(3));
    }

    private void checkRequestOut(LogEvent requestOut) {
        assertEquals(SERVICE_URI, requestOut.getAddress());
        assertEquals("text/xml", requestOut.getContentType());
        assertEquals(EventType.REQ_OUT, requestOut.getType());
        assertEquals(StandardCharsets.UTF_8.name(), requestOut.getEncoding());
        assertNotNull(requestOut.getExchangeId());
        assertEquals("POST", requestOut.getHttpMethod());
        assertNotNull(requestOut.getMessageId());
        assertTrue(requestOut.getPayload().contains("<arg0>test</arg0>"));
        assertEquals("TestServicePort", requestOut.getPortName().getLocalPart());
        assertEquals("TestService", requestOut.getPortTypeName().getLocalPart());
        assertEquals("TestServiceService", requestOut.getServiceName().getLocalPart());
    }
    
    private void checkRequestIn(LogEvent requestIn) {
        assertEquals(SERVICE_URI, requestIn.getAddress());
        assertEquals("text/xml; charset=UTF-8", requestIn.getContentType());
        assertEquals(EventType.REQ_IN, requestIn.getType());
        assertEquals(StandardCharsets.UTF_8.name(), requestIn.getEncoding());
        assertNotNull(requestIn.getExchangeId());
        assertEquals("POST", requestIn.getHttpMethod());
        assertNotNull(requestIn.getMessageId());
        assertTrue(requestIn.getPayload().contains("<arg0>test</arg0>"));
        assertEquals("TestServiceImplementationPort", requestIn.getPortName().getLocalPart());
        assertEquals("TestService", requestIn.getPortTypeName().getLocalPart());
        assertEquals("TestServiceImplementationService", requestIn.getServiceName().getLocalPart());
    }
    
    private void checkResponseOut(LogEvent responseOut) {
        // Not yet available
        assertNull(responseOut.getAddress());
        assertEquals("text/xml", responseOut.getContentType());
        assertEquals(EventType.RESP_OUT, responseOut.getType());
        assertEquals(StandardCharsets.UTF_8.name(), responseOut.getEncoding());
        assertNotNull(responseOut.getExchangeId());
        
        // Not yet available
        assertNull(responseOut.getHttpMethod());
        assertNotNull(responseOut.getMessageId());
        assertTrue(responseOut.getPayload().contains("<return>test</return>"));
        assertEquals("TestServiceImplementationPort", responseOut.getPortName().getLocalPart());
        assertEquals("TestService", responseOut.getPortTypeName().getLocalPart());
        assertEquals("TestServiceImplementationService", responseOut.getServiceName().getLocalPart());
    }
    
    private void checkResponseIn(LogEvent responseIn) {
        assertNull(responseIn.getAddress());
        assertEquals("text/xml;charset=utf-8", responseIn.getContentType().toLowerCase().replace(" ", ""));
        assertEquals(EventType.RESP_IN, responseIn.getType());
        assertEquals(StandardCharsets.UTF_8.name(), responseIn.getEncoding());
        assertNotNull(responseIn.getExchangeId());
        
        // Not yet available
        assertNull(responseIn.getHttpMethod());
        assertNotNull(responseIn.getMessageId());
        assertTrue(responseIn.getPayload().contains("<return>test</return>"));
        assertEquals("TestServicePort", responseIn.getPortName().getLocalPart());
        assertEquals("TestService", responseIn.getPortTypeName().getLocalPart());
        assertEquals("TestServiceService", responseIn.getServiceName().getLocalPart());
    }

    private TestService createTestClient(Feature feature) throws MalformedURLException {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setAddress(SERVICE_URI);
        factory.setFeatures(Collections.singletonList(feature));
        return factory.create(TestService.class);
    }

}
