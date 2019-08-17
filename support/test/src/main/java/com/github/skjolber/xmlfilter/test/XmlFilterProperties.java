package com.github.skjolber.xmlfilter.test;

import java.util.Properties;

import com.github.skjolber.xmlfilter.XmlFilter;

public class XmlFilterProperties {

	private Properties properties;
	private XmlFilter xmlFilter;
	
	public XmlFilterProperties(XmlFilter xmlFilter, Properties properties) {
		this.xmlFilter = xmlFilter;
		this.properties = properties;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof XmlFilterProperties) {
			XmlFilterProperties wrapper = (XmlFilterProperties)obj;
			
			return wrapper.properties.equals(properties);
		}

		return false;
	}
	
	public XmlFilter getXmlFilter() {
		return xmlFilter;
	}
	
	public Properties getProperties() {
		return properties;
	}

	public boolean matches(Properties properties) {
		return this.properties.equals(properties);
	}
	
	public boolean isNoop() {
		return properties == null;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
