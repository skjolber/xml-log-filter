package com.github.skjolber.xmlfilter.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;

import com.github.skjolber.ddom.xmlts.XMLConformanceTest;
import com.github.skjolber.ddom.xmlts.XMLConformanceTestSuite;
import com.github.skjolber.indent.Indent;
import com.github.skjolber.indent.IndentBuilder;
import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.filter.AbstractXmlFilter;
import com.github.skjolber.xmlfilter.test.XmlFilterConformanceRunner;
import com.github.skjolber.xmlfilter.test.XmlFilterConstants;
import com.github.skjolber.xmlfilter.test.XmlFilterProperties;
import com.github.skjolber.xmlfilter.test.XmlFilterPropertiesFactory;

/**
 * 
 * Abstract test class.
 *
 */

public abstract class BaseXmlFilterTest implements XmlFilterConstants {

	protected Indent indent = new IndentBuilder().withTab().withUnixLinebreak().build();

	private XmlFilterConformanceRunner runner;

	public BaseXmlFilterTest() {
		try {
			List<String> nullable = Arrays.asList(PASSTHROUGH_XPATH);
			runner = new XmlFilterConformanceRunner(nullable, new File(BASE_PATH), new XmlFilterPropertiesFactory(nullable) {
				
				@Override
				public XmlFilterProperties createInstance(XmlFilter filter) {
					XmlFilterProperties properties = super.createInstance(filter);
					if(properties.isNoop()) {
						Properties p = new Properties();
						properties.setProperties(p);
					}
					properties.getProperties().put("quote", "neutral");
					properties.getProperties().put("emptyElements", "neutral");
					return properties;
				}
			}, XMLConformanceTestSuite.newInstance(XMLConformanceTestSuite.class.getResource("/xmlts20130923/xmlconf.xml")));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void assertProcess(List<XmlFilter> filters) throws Exception {
		List<File> files = new ArrayList<>();
		for(XmlFilter filter : filters) {
			files.addAll(runner.process(filter));
		}
		Assertions.assertFalse(files.isEmpty());
	}
	
	protected void assertProcess(XmlFilter xmlFilter) throws Exception {
		assertProcess(Arrays.<XmlFilter>asList(xmlFilter));
	}
	
	protected void assertValidXmlConformant(List<XmlFilter> filters) throws Exception {
		List<XMLConformanceTest> files = new ArrayList<>();
		for(XmlFilter filter : filters) {
			files.addAll(runner.processConformance(filter, filter instanceof AbstractXmlFilter && !((AbstractXmlFilter)filter).getXmlDeclaration()));
		}
		Assertions.assertTrue(files.isEmpty());
		
	}
	
	protected void assertValidXmlConformant(XmlFilter xmlFilter) throws Exception {
		assertValidXmlConformant(Arrays.<XmlFilter>asList(xmlFilter));
	}

}
