package com.github.skjolber.xmlfilter.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import com.github.skjolber.ddom.stream.sax.XMLConformanceEntityResolver;
import com.github.skjolber.xmlfilter.XmlFilter;

public class XmlFilterRunner {
	
	private XmlFilterPropertiesFactory xmlFilterPropertiesFactory;
	private XmlFilterOutputDirectoriesFactory xmlOutputDirectoriesFactory;
	private File directory;
	private List<XmlFilterOutputDirectory> outputDirectory;
	private boolean literal = true;;
	
	public XmlFilterRunner(List<?> nullable, File directory, XmlFilterPropertiesFactory xmlFilterPropertiesFactory, boolean literal) throws Exception {
		this.directory = directory;
		this.xmlOutputDirectoriesFactory = new XmlFilterOutputDirectoriesFactory(nullable);
		this.outputDirectory = xmlOutputDirectoriesFactory.create(directory);

		this.xmlFilterPropertiesFactory = xmlFilterPropertiesFactory;
		
		this.literal = literal;
	}

	public List<File> process(XmlFilter xmlFilter) throws Exception {
		List<File> directories = new ArrayList<>();
		
		XmlFilterProperties properties = xmlFilterPropertiesFactory.createInstance(xmlFilter);
		if(!properties.isNoop()) {
			for(XmlFilterOutputDirectory xmlOutputDirectory : outputDirectory) {
				for (XmlFilterInputDirectory xmlInputDirectory : xmlOutputDirectory.getSourceDirectories()) {
					if(xmlInputDirectory.matches(properties)) {
						processInputOutput(xmlInputDirectory, xmlOutputDirectory, xmlFilter);
						
						directories.add(xmlInputDirectory.getDirectory());
					}
				}
			}
		} else {
			processDirectories(directory, xmlFilter, directories);
		}
				
		return directories;
	}

	private void processDirectories(File directory, XmlFilter xmlFilter, List<File> directories) {
		File[] files = XmlFilterInputDirectory.getFiles(directory);
		if(files != null && files.length > 0) {
			for(int i = 0; i < files.length; i++) {
				String from;
				try {
					from = IOUtils.toString(files[i].toURI().toURL(), "UTF-8");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	
				StringBuilder output = new StringBuilder(1024);
				if(!xmlFilter.process(from, output)) {
					System.out.println(from);
					throw new IllegalArgumentException("Unable to process " + files[i] + " using " + xmlFilter);
				}
				String to = output.toString();
	
				if(!new String(from).equals(to)) {
					printDiff(xmlFilter, XmlFilterPropertiesFactory.noopProperties, files[i], files[i], from, to, from);
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
				processDirectories(subdirectory, xmlFilter, directories);
			}
		}
	}

	protected void processInputOutput(XmlFilterInputDirectory inputDirectory, XmlFilterOutputDirectory outputDirectory, XmlFilter xmlFilter) throws Exception {
		File[] filteredFiles = outputDirectory.getFiles();
		File[] sourceFiles = inputDirectory.getFiles();

		for(int i = 0; i < filteredFiles.length; i++) {
			if(!filteredFiles[i].getName().equals(sourceFiles[i].getName())) {
				throw new IllegalArgumentException();
			}
			String from = IOUtils.toString(sourceFiles[i].toURI().toURL(), "UTF-8");
			
			StringBuilder output = new StringBuilder(1024);
			if(!xmlFilter.process(from, output)) {
				throw new IllegalArgumentException("Unable to process " + sourceFiles[i] + " using " + xmlFilter);
			}
			String to = output.toString();

			String expected = IOUtils.toString(filteredFiles[i].toURI().toURL(), "UTF-8");

			if(Character.isWhitespace(to.charAt(0))) {
				// assume indent
				to = to.substring(1);
			}
			
			if(isWellformed(to) != isWellformed(expected)) {
				printDiff(xmlFilter, inputDirectory.getProperties(), filteredFiles[i], sourceFiles[i], from, to, expected);
				throw new IllegalArgumentException("Unexpected result for " + sourceFiles[i]);
			}

			if(literal) {
				if(!new String(expected).equals(to)) {
					printDiff(xmlFilter, inputDirectory.getProperties(), filteredFiles[i], sourceFiles[i], from, to, expected);
					throw new IllegalArgumentException("Unexpected result for " + sourceFiles[i]);
				}
			} else {
				Diff d = DiffBuilder.compare(expected).withTest(to).build();
				if(d.hasDifferences()) {
					printDiff(xmlFilter, inputDirectory.getProperties(), filteredFiles[i], sourceFiles[i], from, to, expected);
					throw new IllegalArgumentException("Unexpected result for " + sourceFiles[i]);
				}
			}

		}
	}

	protected void printDiff(XmlFilter filter, Properties properties, File expectedFile, File original, String from, String to, String expected) {
		System.out.println("Processing using: " + filter);
		System.out.println("Properties: " + properties);
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
	
	public static boolean hasXMLDeclaration(String out) {
		return out.startsWith("<?xml ");
	}

	
	public static boolean isWellformed(String out) {
        SAXParserFactory saxFactory = new SAXParserFactoryImpl();
        saxFactory.setNamespaceAware(false);
		try {
	        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
        
	        xmlReader.parse(new InputSource(new StringReader(out)));
		} catch(Exception e) {
			return false;
		}
		return true;			
	}

	public static boolean isWellformed(final URL base) {
		
        SAXParserFactory saxFactory = new SAXParserFactoryImpl();
        saxFactory.setNamespaceAware(false);
		try {
	        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
	        if(base != null) {
	        	xmlReader.setEntityResolver(new XMLConformanceEntityResolver(base));
	        }
        
	        xmlReader.parse(base.toExternalForm());
		} catch(Exception e) {
			return false;
		}
		return true;
	}
		
}
