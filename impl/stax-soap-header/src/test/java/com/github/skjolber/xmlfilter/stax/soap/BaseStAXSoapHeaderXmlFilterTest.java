package com.github.skjolber.xmlfilter.stax.soap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.codehaus.stax2.XMLOutputFactory2;
import org.junit.Assert;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.test.XmlFilterConstants;
import com.github.skjolber.xmlfilter.test.XmlFilterProperties;
import com.github.skjolber.xmlfilter.test.XmlFilterPropertiesFactory;
import com.github.skjolber.xmlfilter.test.XmlFilterRunner;

/**
 * 
 * Abstract test class.
 *
 */

public abstract class BaseStAXSoapHeaderXmlFilterTest implements XmlFilterConstants {

	private XmlFilterRunner runner;
	protected InputFactoryImpl xmlInputFactory;
	protected OutputFactoryImpl xmlOutputFactory;

	public BaseStAXSoapHeaderXmlFilterTest() {
		try {
			List<String> nullable = Arrays.asList(PASSTHROUGH_XPATH);
			runner = new XmlFilterRunner(nullable, new File(BASE_PATH), new XmlFilterPropertiesFactory(nullable) {
				
				@Override
				public XmlFilterProperties createInstance(XmlFilter filter) {
					XmlFilterProperties properties = super.createInstance(filter);
					if(properties.isNoop()) {
						Properties p = new Properties();
						properties.setProperties(p);
					}
					properties.getProperties().put("quote", "double");
					properties.getProperties().put("emptyElements", "double");
					return properties;
				}
			}, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		xmlInputFactory = new InputFactoryImpl();
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);

		xmlOutputFactory = new OutputFactoryImpl();
		xmlOutputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
		xmlOutputFactory.setProperty(XMLOutputFactory2.P_AUTOMATIC_EMPTY_ELEMENTS, false);		
	}

	protected void assertProcess(List<XmlFilter> filters) throws Exception {
		List<File> files = new ArrayList<>();
		for(XmlFilter filter : filters) {
			files.addAll(runner.process(filter));
		}
		Assert.assertFalse(files.isEmpty());
	}

}
