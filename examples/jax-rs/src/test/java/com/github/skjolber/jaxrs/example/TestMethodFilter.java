package com.github.skjolber.jaxrs.example;


import static org.junit.Assert.assertNotNull;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import com.github.skjolber.jaxrs.example.MyApp;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestMethodFilter extends JerseyTest {

	@Override
	protected ResourceConfig configure() {
		return new MyApp();
	}

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

	@Test
	public void testMyMethod() throws InterruptedException {
		String response = target().path("myResource/myMethod").request("*").get(String.class);
		assertNotNull(response);

		verify(mockAppender, times(2)).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

		assertThat(loggingEvent.getLevel(), is(Level.INFO));
		assertTrue(loggingEvent.getFormattedMessage().contains("Value"));
	}

	@Test
	public void testMyFilterMethod() throws InterruptedException {
		String response = target().path("myResource/myFilterMethod").request("*").get(String.class);
		assertNotNull(response);

		verify(mockAppender, times(2)).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

		assertThat(loggingEvent.getLevel(), is(Level.INFO));
		assertFalse(loggingEvent.getFormattedMessage().contains("Value"));
		assertTrue(loggingEvent.getFormattedMessage().contains("[*****]"));
	}

}