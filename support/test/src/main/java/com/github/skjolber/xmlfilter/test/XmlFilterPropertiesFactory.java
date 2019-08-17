package com.github.skjolber.xmlfilter.test;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import com.github.skjolber.xmlfilter.XmlFilter;

/**
 * 
 * Read bean info and get properties which can be directly compared to properties from a properties-file.
 * 
 */

public class XmlFilterPropertiesFactory extends AbstractXmlFilterPropertiesFactory {

	public XmlFilterPropertiesFactory(List<?> nullable) {
		super(nullable);
	}

	public static Properties noopProperties;

	static {
		noopProperties = new Properties();
		noopProperties.put("xmlDeclaration", true);
		noopProperties.put("quote", "neutral");
	}
	
	public static boolean isFilterDirectory(File directory) {
		return new File(directory, "filter.properties").exists();
	}
	
	public XmlFilterProperties createInstance(XmlFilter filter) {
		try {
			Properties properties = new Properties();
			BeanInfo beanInfo = Introspector.getBeanInfo(filter.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			if(propertyDescriptors == null || propertyDescriptors.length == 1) {
				return new XmlFilterProperties(filter, null);
			}
			for (PropertyDescriptor pd : propertyDescriptors) {
				Method readMethod = pd.getReadMethod();
				if(readMethod != null) {
					Object invoke = readMethod.invoke(filter);
					
					String name = readMethod.getName();
					if(!name.equals("getClass")) {
						put(properties, invoke, normalize(name));
					}
				}
			}
			
			return new XmlFilterProperties(filter, properties);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
