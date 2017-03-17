package com.github.skjolber.ddom.stream.sax;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.github.skjolber.ddom.xmlts.XMLConformanceTest;

/**
 * Entity resolver cache. For working around entity lookups which do not have a reference URL.
 *
 */

public class XMLConformanceMapEntityResolver implements EntityResolver {
	
	public static XMLConformanceMapEntityResolver newInstance(XMLConformanceTest test) throws Exception {

		try {
	        SAXParserFactory saxFactory = new SAXParserFactoryImpl();
	        saxFactory.setNamespaceAware(false);
	        
	        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
	        
	        XMLConformanceMapEntityResolver resolver = new XMLConformanceMapEntityResolver(test.getEntityResolver());
	        
	    	xmlReader.setEntityResolver(resolver);
	    
	        xmlReader.parse(test.getUrl().toExternalForm());
			
	        return resolver;
		} catch(Exception e) {
			return null;
		}
	}

	private Map<String, String> entities = new HashMap<>();
	
	private EntityResolver delegate;

	public XMLConformanceMapEntityResolver(EntityResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		int index = systemId.lastIndexOf('/');
		String fileName = systemId.substring(index+1);
		
		String entity =  entities.get(fileName);
		if(entity == null) {
			InputSource inputSource = delegate.resolveEntity(publicId, systemId);
			entity = IOUtils.toString(inputSource.getCharacterStream());
			entities.put(fileName, entity);
		}
		return new InputSource(new StringReader(entity));
	}

}
