package com.github.skjolber.xmlfilter.cxf.example.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.skjolber.xmlns.schema.logger.LogHeader;
import com.github.skjolber.xmlns.schema.logger.LoggerException;
import com.github.skjolber.xmlns.schema.logger.LoggerPort;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageRequest;
import com.github.skjolber.xmlns.schema.logger.PerformLogMessageResponse;

/**
 * A simple webservice which performs a simple log statement per request.
 *
 */

public class LoggerWebservice implements LoggerPort {

	private static final Logger logger = LoggerFactory.getLogger(LoggerWebservice.class);

	@Override
	public PerformLogMessageResponse performLogMessage(PerformLogMessageRequest parameters, LogHeader logHeader) throws LoggerException {
		logger.info("performLogMessage");
		PerformLogMessageResponse response = new PerformLogMessageResponse();
		response.getAny().addAll(parameters.getAny());
		return response;
	}

}
