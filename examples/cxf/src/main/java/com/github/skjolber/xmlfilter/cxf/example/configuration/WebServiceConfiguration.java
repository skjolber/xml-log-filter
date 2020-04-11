package com.github.skjolber.xmlfilter.cxf.example.configuration;

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.cxf.LoggingAccumulatorInteceptor;
import com.github.skjolber.xmlfilter.cxf.LoggingInInterceptor;
import com.github.skjolber.xmlfilter.cxf.LoggingOutInterceptor;
import com.github.skjolber.xmlfilter.cxf.example.endpoints.LoggerWebservice;
import com.github.skjolber.xmlns.schema.logger.LoggerPort;
import com.github.skjolber.xmlns.schema.logger.LoggerService;

/**
 * CXF webservice configuration.
 *
 */

@Configuration
@PropertySource("classpath:application.properties")
public class WebServiceConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(WebServiceConfiguration.class);

	private static String xpath = "/Envelope/Header/logHeader/sessionId";
	
	@Value("${base.path}")
	private String path;

	@Value("${endpoint.path}")
	private String endpointUrl;

	@Bean
	public ServletRegistrationBean<?> cxfServlet() {
		return new ServletRegistrationBean<>(new CXFServlet(), path + "/*");
	}

	@Bean(name = Bus.DEFAULT_BUS_ID)
	public SpringBus springBus() {
		SpringBus springBus = new SpringBus();
		return springBus;
	}

	@Bean
	public LoggerPort port() {
		return new LoggerWebservice();
	}

	@Bean
	public Endpoint endpoint11() {
		return setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, 128, 128));
	}

	public Endpoint setXmlFilter(XmlFilter xmlFilter) {
		EndpointImpl endpoint = new EndpointImpl(springBus(), port());
		endpoint.setServiceName(service().getServiceName());
		endpoint.setWsdlLocation(service().getWSDLDocumentLocation().toString()); 
		
		LoggingInInterceptor inInterceptor = new LoggingInInterceptor();
		inInterceptor.setXmlFilter(xmlFilter); 
		LoggingOutInterceptor outInterceptor = new LoggingOutInterceptor();
		outInterceptor.setXmlFilter(xmlFilter); 
		
		LoggingAccumulatorInteceptor inLoggingAccumulatorInteceptor = new LoggingAccumulatorInteceptor();
	
		endpoint.getInInterceptors().add(inLoggingAccumulatorInteceptor);
		endpoint.getInInterceptors().add(inInterceptor);
		endpoint.getInFaultInterceptors().add(inInterceptor);
		endpoint.getOutInterceptors().add(outInterceptor);
		endpoint.getOutFaultInterceptors().add(outInterceptor); 
		
		String path = String.format(endpointUrl, xmlFilter.getClass().getSimpleName());
		
		logger.info("Publish at " + path);
		
		endpoint.publish(path);
		return endpoint;
	}

	@Bean
	public LoggerService service() {
		// Needed for correct ServiceName & WSDLLocation to publish contract
		// first incl. original WSDL
		return new LoggerService();
	}

}
