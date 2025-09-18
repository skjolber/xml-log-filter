package com.github.skjolber.xmlfilter.cxf.example.endpoints;

import java.io.IOException;

import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * 
 * A servlet which simply consumes the incoming content and performs a simple log statement per request.
 * 
 * 
 */

public class ReaderServlet extends GenericServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ReaderServlet.class);

	@Override
	public void service(ServletRequest req, ServletResponse servletResponse) throws ServletException, IOException {
		
		ServletInputStream inputStream = req.getInputStream();
		byte[] content = new byte[4 * 1024];
		
		while(inputStream.read(content) != -1);
		
		logger.info("Got request");
		
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		response.setStatus(200);
	}
	
}