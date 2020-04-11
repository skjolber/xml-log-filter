package com.github.skjolber.xmlfilter.cxf.example.endpoints;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

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