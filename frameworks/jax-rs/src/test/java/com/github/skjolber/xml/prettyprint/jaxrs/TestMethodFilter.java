package com.github.skjolber.xml.prettyprint.jaxrs;

import static javax.ws.rs.client.Entity.xml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogContainerFilter;
import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogContainerFilter.CacheStream;
import com.github.skjolber.xmlns.schema.logger.App;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageRequest;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageResponse;

public class TestMethodFilter extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
       return App.createApp1();
    }

    @Test
    public void testPerformLogMessageObjects() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");
        
        PerformLogMessageResponse performLogMessage = target().path("logger1/performLogMessageObject").request("application/xml").post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }

    @Test
    public void testPerformLogMessageRequestObject() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");
        
        Integer status = target().path("logger1/performLogMessageRequestObject").request().post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}), Integer.class);
        assertNotNull(status);
        assertEquals(1, status.intValue());
     }

    @Test
    public void testPerformLogMessageEmptyResponset() {
        PerformLogMessageRequest request = new PerformLogMessageRequest();
        request.setAddress("thomas.skjolberg@gmail.com");
        request.setSubject("Subject");
        request.setBody("Body");
        
        target().path("logger/performLogMessageEmptyResponse").request().post(xml(new GenericEntity<PerformLogMessageRequest>(request) {}));
     }
    @Test
    public void testPerformLogMessageResponseObject() {
        PerformLogMessageResponse performLogMessage = target().path("logger1/performLogMessageResponseObject").request("application/xml").post(Entity.text(new GenericEntity<String>("message") {}), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }
    
    @Test
    public void testPerformLogMessageParameter() {
        PerformLogMessageResponse performLogMessage = target().path("logger1/performLogMessageParameter/message").request().get(PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }
 
    @Test
    public void testPerformLogMessageResponseObjectInvalid() {
        PerformLogMessageResponse performLogMessage = target().path("logger1/performLogMessageResponseObject").request("application/xml").post(Entity.entity(new GenericEntity<String>("<xml>") {}, "text/xml"), PerformLogMessageResponse.class);
        assertNotNull(performLogMessage);
        assertEquals(1, performLogMessage.getStatus());
     }
    
    @Test
    public void testCacheStream() throws IOException {
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();
    	
    	@SuppressWarnings("resource")
		CacheStream cacheStream = new XmlLogContainerFilter.CacheStream(bout);
    	
    	cacheStream.write(0);
    	
    	Assert.assertEquals(1, bout.size());
    	Assert.assertEquals(1, cacheStream.getCacheOutputStream().size());
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
    	Assert.assertSame(entityStream, result);
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
    	
    	Assert.assertTrue(Arrays.equals(content, resultContent));
     }
    
    @Test
    public void testAroundWriteToFails() throws IOException {
    	XmlLogContainerFilter filter = new XmlLogContainerFilter();

        WriterInterceptorContext context = mock(WriterInterceptorContext.class);

        try {
        	filter.aroundWriteTo(context);
        	
        	Assert.fail();
        } catch(Exception e) {
        	// ignore
        }
        
        context = mock(WriterInterceptorContext.class);
        when(context.getProperty(XmlLogContainerFilter.LOG_BUILDER)).thenReturn(new StringBuilder());
        try {
        	filter.aroundWriteTo(context);
        	
        	Assert.fail();
        } catch(Exception e) {
        	// ignore
        }

        context = mock(WriterInterceptorContext.class);
        when(context.getProperty(XmlLogContainerFilter.CACHE_STREAM)).thenReturn(new CacheStream(new ByteArrayOutputStream()));

        try {
        	filter.aroundWriteTo(context);
        	
        	Assert.fail();
        } catch(Exception e) {
        	// ignore
        }

        
    }
     
    @Test
    public void testAroundWriteToMimeTypes() throws IOException {
    	XmlLogContainerFilter filter = new XmlLogContainerFilter();

    	CacheStream cacheStream = new CacheStream(new ByteArrayOutputStream());
    	cacheStream.write("<xml>".getBytes("UTF-8"));
    	cacheStream.close();
    	
        WriterInterceptorContext context = mock(WriterInterceptorContext.class);
        when(context.getProperty(XmlLogContainerFilter.LOG_BUILDER)).thenReturn(new StringBuilder());
        when(context.getProperty(XmlLogContainerFilter.CACHE_STREAM)).thenReturn(cacheStream);
        when(context.getMediaType()).thenReturn(MediaType.TEXT_XML_TYPE);
        
    	filter.aroundWriteTo(context);
        
    }
     

}