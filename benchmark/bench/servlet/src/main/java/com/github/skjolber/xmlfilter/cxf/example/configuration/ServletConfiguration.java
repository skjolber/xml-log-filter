package com.github.skjolber.xmlfilter.cxf.example.configuration;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.codehaus.stax2.XMLOutputFactory2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.xmlfilter.core.DefaultXmlFilter;
import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.MultiXPathXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathAnonymizeXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneMaxNodeLengthXmlFilter;
import com.github.skjolber.xmlfilter.core.SingleXPathPruneXmlFilter;
import com.github.skjolber.xmlfilter.cxf.example.endpoints.LoggerServlet;
import com.github.skjolber.xmlfilter.cxf.example.endpoints.ReaderServlet;
import com.github.skjolber.xmlfilter.stax.SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.SingleXPathPruneMaxNodeLengthStAXXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathAnonymizeStAXSoapHeaderXmlFilter;
import com.github.skjolber.xmlfilter.stax.soap.SingleXPathPruneStAXSoapHeaderXmlFilter;
import com.github.skjolber.xmlns.schema.logger.LoggerService;
import com.skjolberg.xmlfilter.XmlFilter;
import com.skjolberg.xmlfilter.soap.SingleXPathAnonymizeSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.soap.SingleXPathPruneSoapHeaderXmlFilter;

@Configuration
@PropertySource("classpath:application.properties")
public class ServletConfiguration {

	private static String xpath = "/Envelope/Header/logHeader/sessionId";

	private InputFactoryImpl xmlInputFactory;
	private OutputFactoryImpl xmlOutputFactory;

	@Value("${servlet.path}")
	private String servletUrl;

	public ServletConfiguration() {
		xmlInputFactory = new InputFactoryImpl();
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false); 
		
		xmlOutputFactory = new OutputFactoryImpl();
		xmlOutputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		xmlOutputFactory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, false);
	}

	@Bean
	public ServletRegistrationBean endpoint0() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new ReaderServlet(), String.format(servletUrl, "none"));
		bean.setName("none");
		return bean;
	}
	
	@Bean
	public ServletRegistrationBean servlet1() {
		return setXmlFilter(new DefaultXmlFilter());
	}

	@Bean
	public ServletRegistrationBean servlet2() {
		return setXmlFilter(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public ServletRegistrationBean servlet3() {
		return setXmlFilter(new SingleXPathPruneStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public ServletRegistrationBean servlet4() {
		return setXmlFilter(new SingleXPathAnonymizeSoapHeaderXmlFilter(true, xpath, 1));
	}

	@Bean
	public ServletRegistrationBean servlet5() {
		return setXmlFilter(new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, 1));
	}

	@Bean
	public ServletRegistrationBean servlet6() {
		return setXmlFilter(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public ServletRegistrationBean servlet7() {
		return setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
	}

	@Bean
	public ServletRegistrationBean servlet8() {
		return setXmlFilter(new SingleXPathAnonymizeXmlFilter(true, xpath));
	}

	@Bean
	public ServletRegistrationBean servlet9() {
		return setXmlFilter(new SingleXPathPruneXmlFilter(true, xpath));
	}

	@Bean
	public ServletRegistrationBean servlet10() {
		return setXmlFilter(new MultiXPathXmlFilter(false, new String[] { xpath }, null));
	}

	@Bean
	public ServletRegistrationBean servlet11() {
		return setXmlFilter(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, -1));
	}

	@Bean
	public ServletRegistrationBean servlet12() {
		return setXmlFilter(new SingleXPathPruneMaxNodeLengthXmlFilter(true, xpath, -1, -1));
	}

	@Bean
	public ServletRegistrationBean servlet13() {
		return setXmlFilter(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, new String[] { xpath }, null));
	}

	/*
	@Bean
	public ServletRegistrationBean servlet14() throws Exception {
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
	public ServletRegistrationBean servlet15() {
		return setXmlFilter(new MaxNodeLengthXmlFilter(false, -1, -1));
	}

	public ServletRegistrationBean setXmlFilter(XmlFilter xmlFilter) {
		ServletRegistrationBean bean = new ServletRegistrationBean(new LoggerServlet(xmlFilter), String.format(servletUrl, xmlFilter.getClass().getSimpleName()));
		bean.setName(xmlFilter.getClass().getSimpleName());
		return bean;
	}

	@Bean
	public LoggerService service() {
		// Needed for correct ServiceName & WSDLLocation to publish contract
		// first incl. original WSDL
		return new LoggerService();
	}

}
