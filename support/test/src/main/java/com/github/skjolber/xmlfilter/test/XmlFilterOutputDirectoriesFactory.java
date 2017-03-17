package com.github.skjolber.xmlfilter.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class XmlFilterOutputDirectoriesFactory extends AbstractXmlFilterPropertiesFactory {

	public XmlFilterOutputDirectoriesFactory(List<?> nullable) {
		super(nullable);
	}

	public List<XmlFilterOutputDirectory> create(File directory) throws Exception {
		List<XmlFilterOutputDirectory> result = new ArrayList<>();

		processFilterDirectories(directory, 0, result);
		
		return result;
	}
	
	protected void processFilterDirectories(File directory, int level, List<XmlFilterOutputDirectory> results) throws Exception {
		if(XmlFilterPropertiesFactory.isFilterDirectory(directory)) {
			List<File> sourceDirectories = getSourceDirectories(directory, level);

			Properties sourceProperties = new Properties();
			sourceProperties.putAll(readDirectoryProperties(directory));
			
			List<XmlFilterInputDirectory> wrappers = new ArrayList<>();
			for(File sourceDirectory : sourceDirectories) {
				wrappers.add(new XmlFilterInputDirectory(sourceDirectory, clone(sourceProperties)));

				if(XmlFilterPropertiesFactory.isFilterDirectory(sourceDirectory)) {
					// so there will always be at least one more item in the list
					
					// treat xmlDeclaration as a special case; when it is missing, the filter is actually active
					Properties additional = readDirectoryProperties(sourceDirectory);
					boolean declaration = additional.contains("xmlDeclaration") && sourceProperties.containsKey("xmlDeclaration");
					sourceProperties.putAll(additional);
					if(!declaration) {
						sourceProperties.remove("xmlDeclaration");
					}
					// assume at least one more directory
				}
			}			
			results.add(new XmlFilterOutputDirectory(directory, wrappers));
		}
		
		File[] subdirectories = directory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});

		if(subdirectories != null) {
			Arrays.sort(subdirectories);
			for(File subdirectory : subdirectories) {
				processFilterDirectories(subdirectory, level + 1, results);
			}
		}
	}
	
	private Properties clone(Properties sourceProperties) {
		Properties clone = new Properties();
		clone.putAll(sourceProperties);
		return clone;
	}

	private List<File> getSourceDirectories(File directory, int level) {
		List<File> sourceDirectories = new ArrayList<File>();
		File parent = directory.getParentFile();
		do {
			File[] listFiles = XmlFilterInputDirectory.getFiles(parent);
			if(listFiles != null) {
				sourceDirectories.add(parent);
				
				if(!AbstractXmlFilterPropertiesFactory.isFilterDirectory(parent)) {
					break;
				}
			}
			parent = parent.getParentFile();
			
			level--;
			
		} while(parent != null && level >= 0);
		
		return sourceDirectories;
	}
	
	
	private Properties normalize(Properties properties) {
		
		Properties normalizedProperties = new Properties();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			put(normalizedProperties, entry.getValue(), (String)entry.getKey());
		}
		
		return normalizedProperties;
	}
	
	public Properties readDirectoryProperties(File directory) throws IOException {
		return normalize(readPropertiesFile(new File(directory, "filter.properties")));
	}
	
	public Properties readPropertiesFile(File filter) throws IOException {
		Properties properties = new Properties();
		FileInputStream fin = new FileInputStream(filter);
		try {
			properties.load(fin);
		} finally {
			fin.close();
		}
		return properties;
	}

}
