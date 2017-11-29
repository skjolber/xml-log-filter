package com.github.skjolber.jaxrs.example;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class TestMethodFilter {

	@Mock
	private Appender mockAppender;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

	@Before
	public void setup() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.addAppender(mockAppender);
	}

	@After
	public void teardown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.detachAppender(mockAppender);
	}
	
    @Value("${local.server.port}")
    private int port;
    
	@Test
	public void testMyMethod() throws InterruptedException {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:" + this.port);

		Response response = target.path("/rest/myResource/myMethod").request().get(); 
		assertNotNull(response);
		
		verify(mockAppender, times(2)).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

		assertThat(loggingEvent.getLevel(), is(Level.INFO));
		assertTrue(loggingEvent.getFormattedMessage().contains("Value"));
	}
	
	@Test
	public void testMyFilterMethod() throws InterruptedException {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:" + this.port);

		Response response = target.path("/rest/myResource/myFilterMethod").request().get(); 
		assertNotNull(response);
		
		verify(mockAppender, times(2)).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
	
		assertThat(loggingEvent.getLevel(), is(Level.INFO));
		assertFalse(loggingEvent.getFormattedMessage().contains("Value"));
		assertTrue(loggingEvent.getFormattedMessage().contains("[*****]"));
	}    
}