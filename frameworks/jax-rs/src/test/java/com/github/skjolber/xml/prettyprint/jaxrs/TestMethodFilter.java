package com.github.skjolber.xml.prettyprint.jaxrs;

import static jakarta.ws.rs.client.Entity.xml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogContainerFilter.CacheStream;
import com.github.skjolber.xmlfilter.core.DefaultXmlFilter;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageRequest;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageResponse;
import com.github.skjolber.xmlns.schema.logger.SampleRestApplication;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(classes = SampleRestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestMethodFilter {

	@LocalServerPort
	private int port;
	
    @Test
    public void testPerformLogMessageObjects() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");
        
        WebClient wc = WebClient.create("http://localhost:" + port + "/services/logger1");        
        wc.accept("application/xml");
        
        PerformLogMessageResponse performLogMessage = wc.path("performLogMessageObject").post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }

    @Test
    public void testPerformLogMessageRequestObject() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");

        WebClient wc = WebClient.create("http://localhost:" + port + "/services/logger1");

        Integer status = wc.path("performLogMessageRequestObject").post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}), Integer.class);
        assertNotNull(status);
        assertEquals(1, status.intValue());
     }

    @Test
    public void testPerformLogMessageEmptyResponset() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");
        
        WebClient wc = WebClient.create("http://localhost:" + port + "/services/logger1");        
        wc.accept("application/xml");

        wc.path("performLogMessageEmptyResponse").post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}));
     }
    @Test
    public void testPerformLogMessageResponseObject() {
        WebClient wc = WebClient.create("http://localhost:" + port + "/services/logger1");        
        wc.accept("application/xml");

        PerformLogMessageResponse performLogMessage = wc.path("performLogMessageResponseObject").post(Entity.text(new GenericEntity<String>("message") {}), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }
    
    @Test
    public void testPerformLogMessageParameter() {
        WebClient wc = WebClient.create("http://localhost:" + port + "/services/logger1");        
        wc.accept("application/xml");
    	
        PerformLogMessageResponse performLogMessage = wc.path("performLogMessageParameter/message").get(PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }
 
    @Test
    public void testPerformLogMessageResponseObjectInvalid() {
        WebClient wc = WebClient.create("http://localhost:" + port + "/services/logger1");        
        
        PerformLogMessageResponse performLogMessage = wc.path("performLogMessageResponseObject").post(Entity.entity(new GenericEntity<String>("<xml>") {}, "text/xml"), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }
    
    @Test
    public void testCacheStream() throws IOException {
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();
    	
    	@SuppressWarnings("resource")
		CacheStream cacheStream = new XmlLogContainerFilter.CacheStream(bout, new StringBuilder(), new DefaultXmlFilter());
    	
    	cacheStream.write(0);
    	
    	assertEquals(1, bout.size());
    	assertEquals(1, cacheStream.getCacheOutputStream().size());
    }
    
    @Test
    public void testFilter() throws IOException {
        Logger logger = mock(Logger.class);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/path");

    	XmlLogContainerFilter filter = new XmlLogContainerFilter(logger);
    	
    	ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(logger.isInfoEnabled()).thenReturn(Boolean.TRUE);
        
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
        
        when(requestContext.getHeaders()).thenReturn(headers);
        when(requestContext.hasEntity()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
    	filter.filter(requestContext);
    	
        verify(logger, times(1)).info(captor.capture());
    }
    
    @Test
    public void testFilterContentSize() throws IOException {
        Logger logger = mock(Logger.class);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/path");

    	XmlLogContainerFilter filter = new XmlLogContainerFilter(logger);
    	
    	ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(logger.isInfoEnabled()).thenReturn(Boolean.TRUE);
        
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        		
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>();
        ArrayList<Object> list = new ArrayList<Object>();
        list.add("123");
        headers.put("Content-Length", list);
        
        when(responseContext.getHeaders()).thenReturn(headers);
        when(responseContext.hasEntity()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
    	filter.filter(requestContext, responseContext);
    	
        verify(logger, times(1)).info(captor.capture());
    }

    @Test
    public void testFilterNoEntity() throws IOException {
        Logger logger = mock(Logger.class);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getPath()).thenReturn("/path");

    	XmlLogContainerFilter filter = new XmlLogContainerFilter(logger);
    	
    	ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(logger.isInfoEnabled()).thenReturn(Boolean.TRUE);
        
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        		
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>();
        
        when(responseContext.getHeaders()).thenReturn(headers);
        when(responseContext.hasEntity()).thenReturn(Boolean.FALSE);
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        
    	filter.filter(requestContext, responseContext);
    	
        verify(logger, times(1)).info(captor.capture());
    }
    
    
    @Test
    public void testReadInputStreamKnownContentSize() throws IOException {
    	
    	byte[] content = new byte[32 * 1024];
    	for(int i = 0; i < content.length; i++) {
    		content[i] = (byte)(i % 127);
    	}
    	
    	ByteArrayInputStream entityStream = new ByteArrayInputStream(content);
    	
    	StringBuilder builder = new StringBuilder();
    	
    	InputStream result = XmlLogContainerFilter.readInputStream(entityStream, content.length, builder, Charset.forName("UTF-8"));
    	assertSame(entityStream, result);
     }

    @Test
    public void testReadInputStreamUnknownContentSize() throws IOException {
    	
    	byte[] content = new byte[32 * 1024];
    	for(int i = 0; i < content.length; i++) {
    		content[i] = (byte)(i % 127);
    	}
    	
    	ByteArrayInputStream entityStream = new ByteArrayInputStream(content);
    	
    	StringBuilder builder = new StringBuilder();
    	
    	ByteArrayInputStream result = (ByteArrayInputStream) XmlLogContainerFilter.readInputStream(entityStream, -1, builder, Charset.forName("UTF-8"));
    	
    	byte[] resultContent = new byte[32 * 1024];
    	result.read(resultContent);
    	
    	assertTrue(Arrays.equals(content, resultContent));
     }
    
    @Test
    public void testAroundWriteToFails() throws IOException {
    	XmlLogContainerFilter filter = new XmlLogContainerFilter();

        WriterInterceptorContext context = mock(WriterInterceptorContext.class);

        try {
        	filter.aroundWriteTo(context);
        	
        	fail();
        } catch(Exception e) {
        	// ignore
        }
    }
     
    @Test
    public void testAroundWriteToMimeTypes() throws IOException {
    	XmlLogContainerFilter filter = new XmlLogContainerFilter();

    	CacheStream cacheStream = new CacheStream(new ByteArrayOutputStream(), new StringBuilder(), new DefaultXmlFilter());
    	cacheStream.write("<xml>".getBytes("UTF-8"));
    	cacheStream.close();
    	
        WriterInterceptorContext context = mock(WriterInterceptorContext.class);
        when(context.getOutputStream()).thenReturn(cacheStream);
        when(context.getMediaType()).thenReturn(MediaType.TEXT_XML_TYPE);
        
    	filter.aroundWriteTo(context);
    }
     

}