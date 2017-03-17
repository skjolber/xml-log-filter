package com.github.skjolber.xmlfilter.jmh.filter;

import java.io.CharArrayReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.jmh.utils.Jdk8IndentingXMLStreamWriter;
import com.github.skjolber.xmlfilter.jmh.utils.StringBuilderWriter;

public class DefaultStAXXmlIndentationFilter extends AbstractStAXXmlIndentationFilter {

	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	
	public DefaultStAXXmlIndentationFilter(boolean declaration, Indent indent) {
		super(declaration, indent);
	}
	
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
		try {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(new CharArrayReader(chars, offset, length));
			
			XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new StringBuilderWriter(output));
			if(indent != null) {
				writer = new Jdk8IndentingXMLStreamWriter(writer);
			}
			
			copy(reader, writer);
		} catch(Exception e) {
			e.printStackTrace();
			
			return false;
		}
		return true;
	}

}
