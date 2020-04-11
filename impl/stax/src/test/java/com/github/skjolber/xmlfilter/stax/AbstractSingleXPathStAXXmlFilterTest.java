package com.github.skjolber.xmlfilter.stax;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.CharArrayReader;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.jupiter.api.Test;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.filter.AbstractXPathFilter.FilterType;

public class AbstractSingleXPathStAXXmlFilterTest extends SingleXPathStAXXmlFilterTest {

	private static class DefaultSingleXPathStAXXmlFilter extends AbstractSingleXPathStAXXmlFilter {
		
		public DefaultSingleXPathStAXXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type, XMLInputFactory2 inputFactory, XMLOutputFactory2 outputFactory) {
			super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, expression, type, inputFactory, outputFactory);
		}

		@Override
		public boolean process(char[] chars, int offset, int length, StringBuilder output) {
			if(chars.length < offset + length) {
				return false;
			}
			XMLStreamReader2 reader = null;
			XMLStreamWriter2 writer = null;
			try {
				reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(new CharArrayReader(chars, offset, length));
				
				writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(new StringBuilderWriter(output));
				
				move(reader, writer);
			} catch(Exception e) {
				return false;
			} finally {
				if(reader != null) {
					try {
						reader.close();
					} catch (XMLStreamException e) {
					}
				}
				if(writer != null) {
					try {
						writer.close();
					} catch (XMLStreamException e) {
					}
				}
				
			}
			return true;
		}

	};
	
	@Test
	public void testEmpty() {
		assertTrue(AbstractSingleXPathStAXXmlFilter.isEmpty(""));
		assertTrue(AbstractSingleXPathStAXXmlFilter.isEmpty(null));
	}
	
	@Test
	public void testCopy() {
		XmlFilter filter = new DefaultSingleXPathStAXXmlFilter(true, null, -1, -1, DEFAULT_WILDCARD_XPATH, FilterType.PRUNE, xmlInputFactory, xmlOutputFactory);
		
		String xml = "<xml></xml>";
		assertTrue(filter.process(xml.toCharArray(), 0, xml.length(), new StringBuilder()));
	}
}
