package com.skjolberg.xmlfilter.impl.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import com.skjolberg.indent.Indent;
import com.skjolberg.xmlfilter.XmlFilter;

/**
 * 
 * Read bean info and get properties which can be directly compared to properties from a properties-file.
 * 
 */

public class XmlFilterPropertyFactory {

	private final List<?> nullable;
	
	public XmlFilterPropertyFactory(List<?> nullable) {
		this.nullable = nullable;
	}
	
	public Properties createInstanceForDirectory(File directory) throws IOException {
		return createInstance(new File(directory, "filter.properties"));
	}
	
	public Properties createInstance(File filter) throws IOException {
		Properties properties = new Properties();
		FileInputStream fin = new FileInputStream(filter);
		try {
			properties.load(fin);
		} finally {
			fin.close();
		}
		return createInstance(properties);
	}
	
	private Properties createInstance(Properties properties) {
	
		Properties normalizedProperties = new Properties();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			put(normalizedProperties, entry.getValue(), (String)entry.getKey());
		}
		
		return normalizedProperties;
	}

	public Properties createInstance(XmlFilter filter) {
		try {
			Properties properties = new Properties();
			BeanInfo beanInfo = Introspector.getBeanInfo(filter.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			if(propertyDescriptors == null || propertyDescriptors.length == 1) {
				return XmlFilterFactoryFactory.noopProperties;
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
			
			return properties;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void put(Properties properties, Object invoke, String name) {
		Object normalizeValue = normalizeValue(invoke);
		if(normalizeValue != null) {
			Object reducedValue = reduceValue(normalizeValue, nullable);
			if(reducedValue != null) {
				properties.put(name, reducedValue);
			}
		}
	}

	private static Object reduceValue(Object normalizeValue, List<?> nullable) {
		if(nullable.contains(normalizeValue)) {
			return null;
		}
		if(normalizeValue instanceof List) {
			List<Object> output = new ArrayList<>(); 

			List<?> input = (List<?>)normalizeValue; 
			for (Object value : input) {
				Object reducedValue = reduceValue(value, nullable);
				if(reducedValue != null) {
					output.add(reducedValue);
				}
			}
			if(!output.isEmpty()) {
				if(output.size() == 1) {
					return output.get(0);
				}
				return output;
			}
			return null;
		}
		return normalizeValue;
	}

	private static Object normalizeValue(Object invoke) {
		// make sure the object can respond to equals
		if(invoke instanceof String[]) {
			String[] strings = (String[])invoke;
			if(strings.length > 0) {
				return normalizeValue(Arrays.asList(strings));
			}
			return null;
		} else if(invoke instanceof List) {
			List list = (List)invoke;
			if(!list.isEmpty()) {
				if(list.size() == 1) {
					return normalizeValue(list.get(0));
				}
				
				for(int i = 0; i < list.size(); i++) {
					Object value = normalizeValue(list.get(i));
					if(value == null) {
						list.remove(i);
						i--;
					}
				}
				
				return list;
			}
			return null;
		} else if(invoke instanceof Integer) {
			Integer integer = (Integer)invoke;
			if(integer.intValue() == -1 || integer.intValue() == Integer.MAX_VALUE) {
				return null;
			}
			return integer;
		} else if(invoke instanceof Boolean) {
			Boolean integer = (Boolean)invoke;
			if(!integer.booleanValue()) {
				return null;
			}
			return integer;
		} else if(invoke instanceof String) {
			String string = (String)invoke;
			if(string.equals(Boolean.TRUE.toString())) {
				return Boolean.TRUE;
			} else if(string.equals(Boolean.FALSE.toString())) {
				return Boolean.FALSE;
			}
			
			try {
				return normalizeValue(Integer.parseInt(string));
			} catch(NumberFormatException e) {
				// ignore
			}
			
			if(string.contains(",")) {
				return normalizeValue(string.split(","));
			}
					
			return string;
		} else if(invoke instanceof Indent) {
			return Boolean.TRUE;
		}
		
		return invoke;
	}

	private static String normalize(String name) {
		if(name.startsWith("get")) {
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		} else if(name.startsWith("is")) {
			return Character.toLowerCase(name.charAt(2)) + name.substring(3);
		}
		throw new IllegalArgumentException();
	}

}
