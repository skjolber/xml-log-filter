package com.skjolberg.xmlfilter.impl.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;

import com.skjolberg.xmlfilter.XmlFilter;
import com.skjolberg.xmlfilter.core.AbstractXmlFilter;

public class XmlFilterRunner {
	
	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

	private XmlFilterFactoryFactory xmlFilterFactoryFactory;

	public XmlFilterRunner(XmlFilterFactoryFactory xmlFilterFactoryFactory) {
		this.xmlFilterFactoryFactory = xmlFilterFactoryFactory;
	}

	public List<File> processRecursive(File directory, XmlFilter xmlFilter) throws Exception {
		List<File> directories = new ArrayList<>();
		
		Properties properties = xmlFilterFactoryFactory.getPropertyFactory().createInstance(xmlFilter);
		if(properties.equals(XmlFilterFactoryFactory.noopProperties)) {
			// process all files without modifications
			processXmlFiles(directory, xmlFilter, directories);
		} else {
			processFilterDirectories(directory, xmlFilter, directories);
		}
		
		return directories;
	}

	protected void processFilterDirectories(File directory, XmlFilter xmlFilter, List<File> directories) throws Exception {
		if(processFilterDirectory(directory, xmlFilter)) {
			directories.add(directory);
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
				processFilterDirectories(subdirectory, xmlFilter, directories);
			}
		}
	}

	protected void processXmlFiles(File directory, XmlFilter xmlFilter, List<File> directories) throws Exception {
		File[] files = getFiles(directory);
		if(files != null && files.length > 0) {
			for(int i = 0; i < files.length; i++) {
				String from = IOUtils.toString(files[i].toURI().toURL(), "UTF-8");
	
				StringBuilder output = new StringBuilder(1024);
				if(!xmlFilter.process(from, output)) {
					System.out.println(from);
					throw new IllegalArgumentException("Unable to process " + files[i] + " using " + xmlFilter);
				}
				String to = output.toString();
	
				if(!new String(from).equals(to)) {
					printDiff(xmlFilter, files[i], files[i], from, to, from);
					throw new IllegalArgumentException("Unexpected noop result for " + files[i]);
				}
			}				
			directories.add(directory);
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
				processXmlFiles(subdirectory, xmlFilter, directories);
			}
		}
	}

	public boolean processFilterDirectory(File directory, XmlFilter xmlFilter) throws IOException, Exception {
		if(isFilter(directory) ) {
			List<File> sourceDirectories = getSourceDirectories(directory);
			
			Properties properties = xmlFilterFactoryFactory.getPropertyFactory().createInstance(new File(directory, "filter.properties"));
			
			for(File sourceDirectory : sourceDirectories) {
				
				if(matches(xmlFilter, properties)) {
					processFilterFiles(directory, sourceDirectory, xmlFilter);
					
					return true;
				}
				
				if(isFilter(sourceDirectory) ) {
					// treat xmlDeclaration as a special case; when it is missing, the filter is actually active
					Properties additional = xmlFilterFactoryFactory.getPropertyFactory().createInstance(new File(sourceDirectory, "filter.properties"));
					boolean declaration = additional.contains("xmlDeclaration") && properties.containsKey("xmlDeclaration");
					properties.putAll(additional);
					if(!declaration) {
						properties.remove("xmlDeclaration");
					}
				}
			}
			
		}

		return false;
	}
	
	private boolean isFilter(File directory) {
		return new File(directory, "filter.properties").exists();
	}

	protected File[] getFiles(File directory) {
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
	
	protected boolean matches(XmlFilter xmlFilter, Properties properties) throws IOException {
		return xmlFilterFactoryFactory.matches(xmlFilter, properties);
	}
	
	protected boolean matches(XmlFilter xmlFilter, File directory) throws IOException {
		return xmlFilterFactoryFactory.matches(xmlFilter, directory);
	}
	
	protected void processFilterFiles(File outputDirectory, File inputDirectroy, XmlFilter xmlFilter) throws Exception {
		File[] filteredFiles = getFiles(outputDirectory);
		File[] sourceFiles = getFiles(inputDirectroy);

		for(int i = 0; i < filteredFiles.length; i++) {
			if(!filteredFiles[i].getName().equals(sourceFiles[i].getName())) {
				throw new IllegalArgumentException();
			}
			String from = IOUtils.toString(sourceFiles[i].toURI().toURL(), "UTF-8");
			
			StringBuilder output = new StringBuilder(1024);
			if(!xmlFilter.process(from, output)) {
				System.out.println(from);
				throw new IllegalArgumentException("Unable to process " + sourceFiles[i] + " using " + xmlFilter);
			}
			String to = output.toString();

			String expected = IOUtils.toString(filteredFiles[i].toURI().toURL(), "UTF-8");

			if(xmlFilter instanceof AbstractXmlFilter) {
				AbstractXmlFilter filter = (AbstractXmlFilter)xmlFilter;
				if(filter.getIndent() != null && Character.isWhitespace(to.charAt(0))) {
					to = to.substring(1);
				}
			}
			
			
			if(isWellformed(to) != isWellformed(expected)) {
				printDiff(xmlFilter, filteredFiles[i], sourceFiles[i], from, to, expected);
				throw new IllegalArgumentException("Unexpected result for " + sourceFiles[i]);
			}
			
			if(!new String(expected).equals(to)) {
				printDiff(xmlFilter, filteredFiles[i], sourceFiles[i], from, to, expected);
				throw new IllegalArgumentException("Unexpected result for " + sourceFiles[i]);
			}

		}
	}

	private List<File> getSourceDirectories(File directory) {
		List<File> sourceDirectories = new ArrayList<File>();
		File parent = directory.getParentFile();
		do {
			File[] listFiles = getFiles(parent);
			if(listFiles != null) {
				sourceDirectories.add(parent);
				
				if(!isFilter(parent)) {
					break;
				}
			}
			parent = parent.getParentFile();
		} while(parent != null);
		
		return sourceDirectories;
	}
	
	protected void printDiff(XmlFilter filter, File expectedFile, File original, String from, String to, String expected) {
		System.out.println("Processing using: " + filter);
		System.out.println("Properties: " + xmlFilterFactoryFactory.getPropertyFactory().createInstance(filter));
		System.out.println("File input: " + original);
		System.out.println("File expected output: " + expectedFile);
		System.out.println("From: \n" + from);
		System.out.println("Expected:\n" + expected);
		System.out.println("Actual:\n" + to);
		System.out.println("(size " + expected.length() + " vs " + to.length() + ")");
		
		for(int k = 0; k < Math.min(expected.length(), to.length()); k++) {
			if(expected.charAt(k) != to.charAt(k)) {
				System.out.println("Diff at " + k + ": " + expected.charAt(k) + " vs + " + to.charAt(k));
				
				break;
			}
		}
	}
	
	public boolean isWellformed(String out) {
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(out.trim()));
			
			do {
				reader.next();
			} while(reader.hasNext());
		} catch(Exception e) {
			return false;
		}
		return true;
	}
		
	public static boolean hasXMLDeclaration(String out) {
		return out.startsWith("<?xml ");
	}
	
	public XmlFilterFactoryFactory getXmlFilterFactoryFactory() {
		return xmlFilterFactoryFactory;
	}
}
