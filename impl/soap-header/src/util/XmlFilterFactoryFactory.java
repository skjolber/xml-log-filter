package com.skjolberg.xmlfilter.impl.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.skjolberg.indent.IndentBuilder;
import com.skjolberg.xmlfilter.core.DefaultXmlFilterFactory;
import com.skjolberg.xmlfilter.util.XmlFilterPropertiesFactory;
import com.skjolberg.xmlfilter.XmlFilter;

public class XmlFilterFactoryFactory {
	
	public static Properties noopProperties;

	static {
		noopProperties = new Properties();
		noopProperties.put("xmlDeclaration", true);
	}
	
	private XmlFilterPropertiesFactory propertyFactory;
	
	public XmlFilterFactoryFactory(List<?> nullable) {
		this(new XmlFilterPropertiesFactory(nullable));
	}

	public XmlFilterFactoryFactory(XmlFilterPropertiesFactory propertyFactory) {
		this.propertyFactory = propertyFactory;
	}

	public DefaultXmlFilterFactory newInstance(Properties properties) throws IOException {
		DefaultXmlFilterFactory factory = new DefaultXmlFilterFactory();
		
		if(properties.containsKey("xmlDeclaration")) {
			factory.setXmlDeclaration(Boolean.parseBoolean(properties.getProperty("xmlDeclaration")));
		}
		
		if(properties.containsKey("maxTextNodeLength")) {
			factory.setMaxTextNodeLength(Integer.parseInt(properties.getProperty("maxTextNodeLength")));
		}

		if(properties.containsKey("maxCDATANodeLength")) {
			factory.setMaxCDATANodeLength(Integer.parseInt(properties.getProperty("maxCDATANodeLength")));
		}
		
		if(properties.containsKey("pruneFilters")) {
			String filters = properties.getProperty("pruneFilters");
			factory.setPruneFilters(filters.split(","));
		}

		if(properties.containsKey("anonymizeFilters")) {
			String filters = properties.getProperty("anonymizeFilters");
			factory.setAnonymizeFilters(filters.split(","));
		}

		if(properties.containsKey("indent")) {
			String indent = properties.getProperty("indent");
			if(Boolean.parseBoolean(indent)) {
				factory.setIndent(new IndentBuilder().withTab().withUnixLinebreak().build());
			}
		}

		return factory;
	}

	public DefaultXmlFilterFactory newInstanceForDirectory(File directory) throws IOException {
		return newInstance(propertyFactory.createInstanceForDirectory(directory));
	}

	public boolean matches(XmlFilter filter, Properties properties) throws IOException {
		return properties.equals(propertyFactory.createInstance(filter));
	}
	
	public boolean matches(XmlFilter filter, File directory) throws IOException {
		return matches(filter, propertyFactory.createInstanceForDirectory(directory));
	}
	
	public XmlFilterPropertiesFactory getPropertyFactory() {
		return propertyFactory;
	}
}
