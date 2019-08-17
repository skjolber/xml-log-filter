package com.github.skjolber.xmlfilter.servlet.example.endpoints;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.servlet.example.io.CharArrayWriter;

/**
 * A servlet which reads and logs the incoming content. 
 * 
 */

public class LoggerServlet extends GenericServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(LoggerServlet.class);

	private final XmlFilter xmlFilter;
	
	public LoggerServlet(XmlFilter xmlFilter) {
		this.xmlFilter = xmlFilter;
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
		HttpServletResponse response = (HttpServletResponse)servletResponse;

		CharArrayWriter writer = new CharArrayWriter(48 * 1024);
		
		InputStreamReader reader = new InputStreamReader(servletRequest.getInputStream(), "UTF-8");

		char[] content = new char[4 * 1024];
		
		int read;
		do {
			read = reader.read(content, 0, content.length);
			if(read == -1) {
				break;
			}
			writer.write(content, 0, read);
		} while(true);
		
		StringBuilder output = new StringBuilder(writer.size() * 2);
		
		if(xmlFilter.process(writer.getCharArray(), 0, writer.size(), output)) {
			logger.info(output.toString());
		} else {
			logger.info(new String(writer.getCharArray(), 0, writer.size()));
		}
		
		response.setStatus(200);
	}

}
