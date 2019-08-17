package com.github.skjolber.xmlfilter.jmh.filter;

import java.io.CharArrayReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.InputSource;

import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.filter.AbstractXmlFilter;
import com.github.skjolber.xmlfilter.jmh.utils.ContentHandlerToXMLStreamWriter;
import com.github.skjolber.xmlfilter.jmh.utils.Jdk8IndentingXMLStreamWriter;
import com.github.skjolber.xmlfilter.jmh.utils.StringBuilderWriter;

/**
 * 
 * This class is used to define the lower limit of any XML parse + writer approach for pretty printing.
 * 
 * @author thomas
 *
 */

public class XercesSAXXmlIndentationFilter extends AbstractXmlFilter {

	private static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
	private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";

	private SAXParserFactory factory = SAXParserFactory.newInstance();
	private XMLOutputFactory outputFactory = OutputFactoryImpl.newInstance();

	public XercesSAXXmlIndentationFilter(boolean declaration, Indent indent) {
		super(declaration, indent);
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		try {
			factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd" , false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
		try {
			InputSource source = new InputSource(new CharArrayReader(chars, offset, length));

			XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new StringBuilderWriter(output));
			if(indent != null) {
				writer = new Jdk8IndentingXMLStreamWriter(writer);
			}

			ContentHandlerToXMLStreamWriter contentHandlerToXMLStreamWriter = new ContentHandlerToXMLStreamWriter(writer);

			SAXParser saxParser = factory.newSAXParser();

			try {
				saxParser.setProperty(ACCESS_EXTERNAL_DTD, Boolean.FALSE.toString());
			} catch(Exception e) {
				//ignore
			}
			try {
				saxParser.setProperty(ACCESS_EXTERNAL_SCHEMA, Boolean.FALSE.toString());
			} catch(Exception e) {
				//ignore
			}

			saxParser.parse(source, contentHandlerToXMLStreamWriter);
		} catch(Exception e) {
			return false;
		}

		return true;

	}

}

