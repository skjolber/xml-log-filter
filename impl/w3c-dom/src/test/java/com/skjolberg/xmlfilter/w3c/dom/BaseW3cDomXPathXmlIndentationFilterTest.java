package com.skjolberg.xmlfilter.w3c.dom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


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

public abstract class BaseW3cDomXPathXmlIndentationFilterTest implements XmlFilterConstants {

	private XmlFilterRunner runner;

	public BaseW3cDomXPathXmlIndentationFilterTest() {
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
			}, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void assertProcess(List<XmlFilter> filters) throws Exception {
		List<File> files = new ArrayList<>();
		for(XmlFilter filter : filters) {
			files.addAll(runner.process(filter));
		}
		assertFalse(files.isEmpty());
	}

}
