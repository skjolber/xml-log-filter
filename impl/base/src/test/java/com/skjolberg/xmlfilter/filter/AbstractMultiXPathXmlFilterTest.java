package com.skjolberg.xmlfilter.filter;

import static com.skjolberg.xmlfilter.filter.XPathExpressions.INVALID_XPATH;
import static com.skjolberg.xmlfilter.filter.XPathExpressions.PASSTHROUGH_XPATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.skjolber.indent.Indent;

public class AbstractMultiXPathXmlFilterTest {


	private class DefaultMultiXPathXmlFilter extends MultiCharArrayXPathFilter {

		public DefaultMultiXPathXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes) {
			super(declaration, maxTextNodeLength, maxCDATANodeLength, anonymizes, prunes, indentation);
		}
		
		@Override
		public boolean process(char[] chars, int offset, int length, StringBuilder output) {
			output.append(chars, offset, length);
			
			return true;
		}

		@Override
		public CharArrayFilter ranges(char[] chars, int offset, int length) {
			return new CharArrayFilter();
		}
		
	};

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void construct_invalidAnonymizeXPath_throwsException() {
		exception.expect(IllegalArgumentException.class);

		new DefaultMultiXPathXmlFilter(true, null, -1, -1, new String[]{INVALID_XPATH}, null);
	}
	
	@Test
	public void construct_invalidPruneXPath_throwsException() {
		exception.expect(IllegalArgumentException.class);

		new DefaultMultiXPathXmlFilter(true, null, -1, -1, null, new String[]{INVALID_XPATH});
	}
	
	@Test
	public void construct_validPruneXPath_constructed() {
		DefaultMultiXPathXmlFilter xmlFilter = new DefaultMultiXPathXmlFilter(true, null, -1, -1, null, new String[]{PASSTHROUGH_XPATH});
		
		assertThat(xmlFilter.getPruneFilters()[0], is(PASSTHROUGH_XPATH));
		assertThat(xmlFilter.getAnonymizeFilters().length, is(0));
	}

	@Test
	public void construct_validAnonymizeXPath_constructed() {
		DefaultMultiXPathXmlFilter xmlFilter = new DefaultMultiXPathXmlFilter(true, null, -1, -1, new String[]{PASSTHROUGH_XPATH}, null);
		
		assertThat(xmlFilter.getAnonymizeFilters()[0], is(PASSTHROUGH_XPATH));
		assertThat(xmlFilter.getPruneFilters().length, is(0));
		
	}	
}
