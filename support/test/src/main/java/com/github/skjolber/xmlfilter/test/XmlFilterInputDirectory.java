package com.github.skjolber.xmlfilter.test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;

public class XmlFilterInputDirectory {

	public static File[] getFiles(File directory) {
		File[] files = directory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		
		if(files == null || files.length == 0) {
			return null;
		}
		Arrays.sort(files);
		return files;

	}
	
	protected final Properties properties;
	protected final File directory;
	
	public XmlFilterInputDirectory(File directory, Properties properties) {
		this.directory = directory;
		this.properties = properties;
	}

	public boolean matches(XmlFilterProperties wrapper) {
		if(wrapper.getProperties().equals(properties)) {
			return true;
		}

		if(wrapper.getProperties().keySet().equals(properties.keySet())) {
			// use * as ignore
			Properties wrapperProperties = wrapper.getProperties();
			for (Entry<Object, Object> entry : properties.entrySet()) {
				if(!entry.getValue().equals("*")) {
					if(!entry.getValue().equals(wrapperProperties.get(entry.getKey()))) {
						return false;
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	public File[] getFiles() {
		return getFiles(directory);
	}

	public boolean hasProperties() {
		return properties != null;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public File getDirectory() {
		return directory;
	}
	
}
