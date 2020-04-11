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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.Test;

public class RESTLoggingTest {

    private static final String SERVICE_URI = "http://localhost:5679/testrest";
    private static final String SERVICE_URI_BINARY = "http://localhost:5680/testrest";

    @Test
    public void testSlf4j() throws IOException {
        LoggingFeature loggingFeature = new LoggingFeature();
        Server server = createService(loggingFeature);
        server.start();
        WebClient client = createClient(loggingFeature);
        String result = client.get(String.class);
        server.destroy();
        assertEquals("test1", result);
    }
    
    @Test
    public void testBinary() throws IOException {
        LoggingFeature loggingFeature = new LoggingFeature();
        
        TestEventSender sender = new TestEventSender();
        loggingFeature.setSender(sender);
        Server server = createServiceBinary(loggingFeature);
        server.start();
        WebClient client = createClientBinary(loggingFeature);
        client.get(InputStream.class).close();
        
        assertLogged(sender.getEvents().get(0));
        assertLogged(sender.getEvents().get(1));
        assertNotLogged(sender.getEvents().get(2));
        assertNotLogged(sender.getEvents().get(3));

        loggingFeature.setLogBinary(true);
        client.get(InputStream.class).close();
        server.destroy();

        assertLogged(sender.getEvents().get(4));
        assertLogged(sender.getEvents().get(5));
        assertLogged(sender.getEvents().get(6));
        assertLogged(sender.getEvents().get(7));
    }

    private void assertLogged(LogEvent event) {
        assertNotEquals(event.getPayload(), "--- Content suppressed ---", event.getPayload());
    }
    
    private void assertNotLogged(LogEvent event) {
        assertEquals(event.getPayload(), "--- Content suppressed ---", event.getPayload());
    }

    private WebClient createClient(LoggingFeature loggingFeature) {
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setAddress(SERVICE_URI + "/test1");
        bean.setFeatures(Collections.singletonList(loggingFeature));
        return bean.createWebClient();
    }

    private Server createService(LoggingFeature loggingFeature) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress(SERVICE_URI);
        factory.setFeatures(Collections.singletonList(loggingFeature));
        factory.setServiceBean(new TestServiceRest());
        return factory.create();
    }
    
    private WebClient createClientBinary(LoggingFeature loggingFeature) {
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setAddress(SERVICE_URI_BINARY + "/test1");
        bean.setFeatures(Collections.singletonList(loggingFeature));
        return bean.createWebClient();
    }
    
    private Server createServiceBinary(LoggingFeature loggingFeature) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress(SERVICE_URI_BINARY);
        factory.setFeatures(Collections.singletonList(loggingFeature));
        factory.setServiceBean(new TestServiceRestBinary());
        return factory.create();
    }
    
    @Test
    public void testEvents() throws MalformedURLException {
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setLogBinary(true);
        TestEventSender sender = new TestEventSender();
        loggingFeature.setSender(sender);
        Server server = createService(loggingFeature);
        server.start();
        WebClient client = createClient(loggingFeature);
        String result = client.get(String.class);
        assertEquals("test1", result);
        server.destroy();
        List<LogEvent> events = sender.getEvents();
        assertEquals(4, events.size());
        
        checkRequestOut(events.get(0));
        checkRequestIn(events.get(1));
        checkResponseOut(events.get(2));
        checkResponseIn(events.get(3));
    }
    
    private void checkRequestOut(LogEvent requestOut) {
        assertEquals(SERVICE_URI + "/test1", requestOut.getAddress());
        assertNull(requestOut.getContentType());
        assertEquals(EventType.REQ_OUT, requestOut.getType());
        assertNull(requestOut.getEncoding());
        assertNotNull(requestOut.getExchangeId());
        assertEquals("GET", requestOut.getHttpMethod());
        assertNotNull(requestOut.getMessageId());
        assertEquals("", requestOut.getPayload());
    }

    private void checkRequestIn(LogEvent requestIn) {
        assertEquals(SERVICE_URI + "/test1", requestIn.getAddress());
        assertNull(requestIn.getContentType());
        assertEquals(EventType.REQ_IN, requestIn.getType());
        assertNull(requestIn.getEncoding());
        assertNotNull(requestIn.getExchangeId());
        assertEquals("GET", requestIn.getHttpMethod());
        assertNotNull(requestIn.getMessageId());
        assertEquals("", requestIn.getPayload());
    }
    
    private void checkResponseOut(LogEvent responseOut) {
        // Not yet available
        assertNull(responseOut.getAddress());
        assertEquals("application/octet-stream", responseOut.getContentType());
        assertEquals(EventType.RESP_OUT, responseOut.getType());
        assertNull(responseOut.getEncoding());
        assertNotNull(responseOut.getExchangeId());
        
        // Not yet available
        assertNull(responseOut.getHttpMethod());
        assertNotNull(responseOut.getMessageId());
        assertEquals("test1", responseOut.getPayload());
    }
    
    private void checkResponseIn(LogEvent responseIn) {
        // Not yet available
        assertNull(responseIn.getAddress());
        assertEquals("application/octet-stream", responseIn.getContentType());
        assertEquals(EventType.RESP_IN, responseIn.getType());
        assertEquals("ISO-8859-1", responseIn.getEncoding());
        assertNotNull(responseIn.getExchangeId());
        
        // Not yet available
        assertNull(responseIn.getHttpMethod());
        assertNotNull(responseIn.getMessageId());
        assertEquals("test1", responseIn.getPayload());
    }
    

}
