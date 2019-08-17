package com.github.skjolber.xmlfilter.test;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.github.skjolber.ddom.collections.AndFilter;
import com.github.skjolber.ddom.stream.sax.XMLConformanceMapEntityResolver;
import com.github.skjolber.ddom.xmlts.Filters;
import com.github.skjolber.ddom.xmlts.XMLConformanceTest;
import com.github.skjolber.ddom.xmlts.XMLConformanceTestSuite;
import com.github.skjolber.xmlfilter.XmlFilter;

public class XmlFilterConformanceRunner extends XmlFilterRunner {

	private static final Logger logger = LoggerFactory.getLogger(XmlFilterConformanceRunner.class);
	
	public XmlFilterConformanceRunner(List<?> nullable, File directory, XmlFilterPropertiesFactory xmlFilterPropertiesFactory, XMLConformanceTestSuite xmlConformanceTestSuite) throws Exception {
		super(nullable, directory, xmlFilterPropertiesFactory, true);
		
		this.xmlConformanceTestSuite = xmlConformanceTestSuite;
	}

	private XMLConformanceTestSuite xmlConformanceTestSuite;
	
	public static String getXmlDeclarationVersion(String input) {
		int index = input.indexOf("?>");
		
		String declaration = input.substring(0, index + 2).replaceAll("\\s+","");

		if(declaration.contains("version=\"1.1\"") || declaration.contains("version='1.1'")) {
			return "1.1";
		}

		if(declaration.contains("version=\"1.0\"") || declaration.contains("version='1.0'")) {
			return "1.0";
		}

		throw new IllegalArgumentException(input);
	}

	public List<XMLConformanceTest> processConformance(XmlFilter filter, boolean xmlDeclaration) throws Exception {
		
		List<XMLConformanceTest> failed = new ArrayList<>();
        for (XMLConformanceTest test : xmlConformanceTestSuite.getTests(new AndFilter<XMLConformanceTest>(Filters.DEFAULT))) {

	    	String input = test.getInputString();
	    	String output = filter.process(input);
	    	        	
	    	if(output == null) {
				logger.info("********************************************");
	    		logger.info("Processing failed for " + test.getUrl());
	    		logger.info("********************************************");
	    		logger.info(input);
	    		logger.info("********************************************");
	    		
	    		failed.add(test);
	    		
	    		continue;
	    	}
	    	output = output.trim();
	    	
	    	if(xmlDeclaration && XmlFilterRunner.hasXMLDeclaration(input)) {
	    		String xmlVersion = getXmlDeclarationVersion(input);
	    		if(xmlVersion.equals("1.1")) {
	    			// append declaration - some test cases have xml 1.1-feature which must enabled
		    		int first = input.indexOf("?>");
		    		output = input.substring(0, first + 2) + output;
	    		}
	    	}
	    	
	    	XMLConformanceMapEntityResolver resolver = XMLConformanceMapEntityResolver.newInstance(test);
	    	if(resolver != null) {
	    		
	    		SAXParserFactory saxFactory = new SAXParserFactoryImpl();
    	        saxFactory.setNamespaceAware(false);
    			try {
    		        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
		        	xmlReader.setEntityResolver(resolver);
    	        
    		        xmlReader.parse(new InputSource(new StringReader(output)));
    			} catch(Exception e) {
    				logger.info("********************************************");
    	    		logger.info("Wellform document broken for " + test.getUrl(), e);
    	    		logger.info("********************************************");
    	    		logger.info(input);
    	    		logger.info("********************************************");
    	    		logger.info(output);
    	    		logger.info("********************************************");
    				
    	    		failed.add(test);
    			}
	    		
	    	} else {
	    		logger.warn("Unable to parse " + input);
	    	}
        }
        
        return failed;
	}

}
