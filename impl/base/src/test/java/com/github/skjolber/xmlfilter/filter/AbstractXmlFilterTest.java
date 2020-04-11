package com.github.skjolber.xmlfilter.filter;
import static com.github.skjolber.xmlfilter.filter.XPathExpressions.INVALID_XPATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.filter.AbstractXmlFilter;
import com.github.skjolber.xmlfilter.filter.AbstractXPathXmlFilterTest.DefaultXPathXmlFilter;
import com.github.skjolber.xmlfilter.test.XmlFilterRunner;;
public class AbstractXmlFilterTest {

	private class DefaultXmlFilter extends AbstractXmlFilter {


		public DefaultXmlFilter(boolean declaration, Indent indentation) {
			super(declaration, indentation);
		}

		public DefaultXmlFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, int maxLevel) {
			super(declaration, null, maxTextNodeLength, maxCDATANodeLength);
		}

		@Override
		public boolean process(char[] chars, int offset, int length, StringBuilder output) {
			if(XmlFilterRunner.isWellformed(new String(chars, offset, length))) {
				output.append(chars, offset, length);
				
				return true;
			}
			return false;
		}
		
	};
	
	private static String xmlString = "<xml/>";
	private static char[] xmlChars = xmlString.toCharArray();

	private static String invalidXmlString = "<xml>";

	@Test
	public void construct_invalidTextNodeLength_throwsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultXmlFilter(true, -2, Integer.MAX_VALUE, Integer.MAX_VALUE);
		});
	}
	
	@Test
	public void construct_invalidCDataNodeLength_throwsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultXmlFilter(true, Integer.MAX_VALUE, -2, Integer.MAX_VALUE);
		});
	}
	
	@Test
	public void construct_normalInstance_correctValuesStored1() {
		DefaultXmlFilter defaultXmlFilter = new DefaultXmlFilter(true, 1, 2, Integer.MAX_VALUE);
		
		assertThat(defaultXmlFilter.getMaxTextNodeLength(), is(1));
		assertThat(defaultXmlFilter.getMaxCDATANodeLength(), is(2));
	}
	
	@Test
	public void construct_normalInstance_correctValuesStored2() {
		DefaultXmlFilter defaultXmlFilter = new DefaultXmlFilter(true, null);
		
		assertThat(defaultXmlFilter.getMaxTextNodeLength(), is(-1));
		assertThat(defaultXmlFilter.getMaxCDATANodeLength(), is(-1));
	}
	
	@Test
	public void process_validXML_returnsString() {
		DefaultXmlFilter defaultXmlFilter = new DefaultXmlFilter(false, -1, -1, Integer.MAX_VALUE);
		
		assertThat(defaultXmlFilter.getXmlDeclaration(), is(false));
		assertThat(defaultXmlFilter.process(xmlString), is(xmlString));
		assertThat(defaultXmlFilter.process(xmlChars), is(xmlString));
	}	

	@Test
	public void process_invalidXML_returnsNull() {
		DefaultXmlFilter defaultXmlFilter = new DefaultXmlFilter(true, -1, -1, Integer.MAX_VALUE);
		
		assertThat(defaultXmlFilter.getXmlDeclaration(), is(true));
		Assertions.assertNull(defaultXmlFilter.process(invalidXmlString));
		Assertions.assertNull(defaultXmlFilter.process(invalidXmlString));
	}	

	@Test
	public void process_validXML_returnsTrue() throws IOException {
		DefaultXmlFilter defaultXmlFilter = new DefaultXmlFilter(true, -1, -1, Integer.MAX_VALUE);
		
		assertThat(defaultXmlFilter.getXmlDeclaration(), is(true));
		assertThat(defaultXmlFilter.process(xmlString, new StringBuilder(1024)), is(true));
		assertThat(defaultXmlFilter.process(new StringReader(xmlString), xmlString.length(), new StringBuilder(1024)), is(true));
	}		
	
	@Test
	public void process_invalidXML_returnsFalse() throws IOException {
		DefaultXmlFilter defaultXmlFilter = new DefaultXmlFilter(true, -1, -1, Integer.MAX_VALUE);
		
		assertThat(defaultXmlFilter.getXmlDeclaration(), is(true));
		Assertions.assertFalse(defaultXmlFilter.process(invalidXmlString, new StringBuilder(1024)));
		Assertions.assertFalse(defaultXmlFilter.process(new StringReader(invalidXmlString), invalidXmlString.length(), new StringBuilder(1024)));
	}
	
	@Test
	public void process_invalidCharacterLengths_returnsFalse() throws IOException {
		Assertions.assertThrows(EOFException.class, () -> {
			DefaultXmlFilter defaultXmlFilter = new DefaultXmlFilter(true, -1, -1, Integer.MAX_VALUE);
			
			defaultXmlFilter.process(new StringReader(""), 1, new StringBuilder(1024));
		});
	}

}
