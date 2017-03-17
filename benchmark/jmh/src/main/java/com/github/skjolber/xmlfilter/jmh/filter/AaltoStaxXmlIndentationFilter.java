package com.github.skjolber.xmlfilter.jmh.filter;

import java.io.CharArrayReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.jmh.utils.Jdk8IndentingXMLStreamWriter;
import com.github.skjolber.xmlfilter.jmh.utils.StringBuilderWriter;

public class AaltoStaxXmlIndentationFilter extends AbstractStAXXmlIndentationFilter {

	public AaltoStaxXmlIndentationFilter(boolean declaration, Indent indent) {
		super(declaration, indent);
	}

	private XMLInputFactory inputFactory = InputFactoryImpl.newInstance();
	private XMLOutputFactory outputFactory = OutputFactoryImpl.newInstance();

	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
		
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(new CharArrayReader(chars, offset, length));
		
			XMLStreamWriter writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(new StringBuilderWriter(output));
			
			if(indent != null) {
				writer = new Jdk8IndentingXMLStreamWriter(writer);
			}			
			
			copy(reader, writer);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
}
