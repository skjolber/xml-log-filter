package com.github.skjolber.xmlfilter.filter;

import static com.github.skjolber.xmlfilter.filter.XPathExpressions.INVALID_XPATH;
import static com.github.skjolber.xmlfilter.filter.XPathExpressions.PASSTHROUGH_XPATH;
import static com.github.skjolber.xmlfilter.filter.XPathExpressions.PASSTHROUGH_XPATH_ATTRIBUTE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.filter.AbstractXPathFilter.FilterType;

public class SingleXPathFilterTest {

	public class DefaultXPathXmlFilter extends SingleXPathFilter {

		public DefaultXPathXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type) {
			super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, expression, type);
		}

		@Override
		public boolean process(char[] chars, int offset, int length, StringBuilder output) {
			output.append(chars, offset, length);
			
			return true;
		}
		
	};

	@Test
	public void construct_invalidAnonymizeXPath_throwsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultXPathXmlFilter(true, null, -1, -1, INVALID_XPATH, FilterType.ANON);
		});
	}
	
	@Test
	public void construct_invalidPruneXPath_throwsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultXPathXmlFilter(true, null, -1, -1, INVALID_XPATH, FilterType.PRUNE);
		});

	}
	
	@Test
	public void construct_validPruneXPath_constructed() {
		DefaultXPathXmlFilter xmlFilter = new DefaultXPathXmlFilter(true, null, -1, -1, PASSTHROUGH_XPATH, FilterType.PRUNE);
		
		assertThat(xmlFilter.getPruneFilters()[0], is(PASSTHROUGH_XPATH));
		assertThat(xmlFilter.getAnonymizeFilters().length, is(0));
	}

	@Test
	public void construct_validAnonymizeXPath_constructed1() {
		for(String xpath : new String[] {PASSTHROUGH_XPATH, PASSTHROUGH_XPATH_ATTRIBUTE}) {
			DefaultXPathXmlFilter xmlFilter = new DefaultXPathXmlFilter(true, null, -1, -1, xpath, FilterType.ANON);
			
			assertThat(xmlFilter.getAnonymizeFilters()[0], is(xpath));
			assertThat(xmlFilter.getPruneFilters().length, is(0));
		}
	}

	@Test
	public void construct_toString() {
		DefaultXPathXmlFilter xmlFilter = new DefaultXPathXmlFilter(true, null, -1, -1, PASSTHROUGH_XPATH, FilterType.ANON);
		
		assertThat(xmlFilter.toString(), StringContains.containsString(PASSTHROUGH_XPATH));
	}
	
}
