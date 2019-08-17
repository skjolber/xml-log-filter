package com.github.skjolber.xmlfilter.cxf.example.configuration;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.codehaus.stax2.XMLOutputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.core.DefaultXmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneXmlFilter;
import com.github.skjolber.xmlfilter.cxf.LoggingAccumulatorInteceptor;
import com.github.skjolber.xmlfilter.cxf.LoggingInInterceptor;
import com.github.skjolber.xmlfilter.cxf.LoggingOutInterceptor;
import com.github.skjolber.xmlfilter.cxf.example.endpoints.LoggerWebservice;
import com.github.skjolber.xmlfilter.stax.SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.SingleXPathPruneMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathAnonymizeStAXSoapHeaderXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathPruneStAXSoapHeaderXmlFilter;
import com.github.skjolber.xmlns.schema.logger.LoggerPort;
import com.github.skjolber.xmlns.schema.logger.LoggerService;
import com.skjolberg.xmlfilter.soap.SingleXPathAnonymizeSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.soap.SingleXPathPruneSoapHeaderXmlFilter;

/**
 * CXF webservice configuration.
 *
 */


@Configuration
@PropertySource("classpath:application.properties")
public class WebServiceConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(WebServiceConfiguration.class);

	private static String xpath = "/Envelope/Header/logHeader/sessionId";

	private InputFactoryImpl xmlInputFactory;
	private OutputFactoryImpl xmlOutputFactory;
	
	@Value("${base.path}")
	private String path;

	@Value("${endpoint.path}")
	private String endpointUrl;

	public WebServiceConfiguration() {
		xmlInputFactory = new InputFactoryImpl();
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false); 
		
		xmlOutputFactory = new OutputFactoryImpl();
		xmlOutputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		xmlOutputFactory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, false);
	}

	@Bean
	public ServletRegistrationBean cxfServlet() {
		return new ServletRegistrationBean(new CXFServlet(), path + "/*");
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
	public Endpoint endpoint0() {
		EndpointImpl endpoint = new EndpointImpl(springBus(), port());
		endpoint.setServiceName(service().getServiceName());
		endpoint.setWsdlLocation(service().getWSDLDocumentLocation().toString()); 
		
		endpoint.publish(String.format(endpointUrl, "none"));
		return endpoint;
	}
	
	@Bean
	public Endpoint endpoint1() {
		return setXmlFilter(new DefaultXmlFilter());
	}

	@Bean
	public Endpoint endpoint2() {
		return setXmlFilter(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public Endpoint endpoint3() {
		return setXmlFilter(new SingleXPathPruneStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public Endpoint endpoint4() {
		return setXmlFilter(new SingleXPathAnonymizeSoapHeaderXmlFilter(true, xpath, 1));
	}

	@Bean
	public Endpoint endpoint5() {
		return setXmlFilter(new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, 1));
	}

	@Bean
	public Endpoint endpoint6() {
		return setXmlFilter(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public Endpoint endpoint7() {
		return setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public Endpoint endpoint8() {
		return setXmlFilter(new SingleXPathAnonymizeXmlFilter(true, xpath));
	}

	@Bean
	public Endpoint endpoint9() {
		return setXmlFilter(new SingleXPathPruneXmlFilter(true, xpath));
	}

	@Bean
	public Endpoint endpoint10() {
		return setXmlFilter(new MultiXPathXmlFilter(false, new String[] { xpath }, null));
	}

	@Bean
	public Endpoint endpoint11() {
		return setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, -1));
	}

	@Bean
	public Endpoint endpoint12() {
		return setXmlFilter(new SingleXPathPruneMaxNodeLengthXmlFilter(true, xpath, -1, -1));
	}

	@Bean
	public Endpoint endpoint13() {
		return setXmlFilter(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, new String[] { xpath }, null));
	}

	/*
	@Bean
	public Endpoint endpoint14() throws Exception {
		// DOM
		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		
		String[] anon = new String[]{xpath};
		
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter filter = factory.getFilter(context, null, anon);
		
		return setXmlFilter(new W3cDomXPathXmlIndentationFilter(false, false, filter));
	}
	*/
	
	@Bean
	public Endpoint endpoint15() {
		return setXmlFilter(new MaxNodeLengthXmlFilter(false, -1, -1));
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
