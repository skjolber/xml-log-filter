package com.skjolberg.xmlfilter.w3c.dom;

import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.skjolberg.xmlfilter.XmlFilter;

public class W3cDomXPathXmlFilter implements XmlFilter {

	private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private final DocumentBuilder documentBuilder;
	private final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
	private final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
	private final LSSerializer writer = impl.createLSSerializer();
	
	private final XPathFilter filter;
	private final boolean declaration;
	private final boolean indent;
	
	public W3cDomXPathXmlFilter(boolean declaration, boolean indent, XPathFilter filter) throws Exception {
		this.declaration = declaration;
		this.indent = indent;
    	this.filter = filter;
    	
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(false);
		try {
			documentBuilderFactory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd" , false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		documentBuilder = documentBuilderFactory.newDocumentBuilder();

		if(indent) {
			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
		}
		if(!declaration) {
			writer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE); // Set this to true if the declaration is needed to be outputted.
		}
	}
	
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
	    try {
			final InputSource src = new InputSource(new CharArrayReader(chars, offset, length));
			
			Document document = documentBuilder.parse(src);

			filter.filter(document);
			
			output.append(writer.writeToString(document.getDocumentElement()));
	    } catch (Exception e) {
	    	return false;
	    }
	    return true;
	}
	
	public boolean process(String xmlString, StringBuilder output) {
		
		char[] chars = xmlString.toCharArray();
		
		return process(chars, 0, chars.length, output);
	}
	
	public String process(char[] chars) {
		
		StringBuilder output = new StringBuilder(chars.length);
		
		if(process(chars, 0, chars.length, output)) {
			return output.toString();
		}
		return null;
	}
	
	public String process(String xmlString) {
		return process(xmlString.toCharArray());
	}
	
	
	public boolean process(Reader reader, int length, StringBuilder output) throws IOException {
		char[] chars = new char[length];

		int offset = 0;
		int read;
		do {
			read = reader.read(chars, offset, length - offset);
			if(read == -1) {
				throw new EOFException("Expected reader with " + length + " characters");
			}

			offset += read;
		} while(offset < length);

		boolean success = process(chars, 0, chars.length, output);
		
		return success;
	}
	
	public boolean getXmlDeclaration() {
		return declaration;
	}
	
	public boolean getIndent() {
		return indent;
	}
	
	public String getIndentCharacter() {
		return "space";
	}
	
	public int getIndentCount() {
		return 4;
	}
}
