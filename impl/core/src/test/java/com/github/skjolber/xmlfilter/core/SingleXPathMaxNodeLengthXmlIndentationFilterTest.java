package com.github.skjolber.xmlfilter.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.skjolberg.xmlfilter.XmlFilter;
import com.skjolberg.xmlfilter.filter.AbstractXPathFilter.FilterType;

public class SingleXPathMaxNodeLengthXmlIndentationFilterTest extends BaseXmlFilterTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void filter_prune() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathMaxNodeLengthXmlIndentationFilter(true, xpath, FilterType.PRUNE, -1, -1, indent));
			filters.add(new SingleXPathMaxNodeLengthXmlIndentationFilter(false, xpath, FilterType.PRUNE, -1, -1, indent));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_anon() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathMaxNodeLengthXmlIndentationFilter(true, xpath, FilterType.ANON, -1, -1, indent));
			filters.add(new SingleXPathMaxNodeLengthXmlIndentationFilter(false, xpath, FilterType.ANON, -1, -1, indent));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_textWithAny_throwsException() throws Exception {
		exception.expect(IllegalArgumentException.class);

		new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_ANY_XPATH, FilterType.PRUNE, -1, -1, indent);
	}
	
	@Test
	public void filter_pruneTextWithAttribute_throwsException() throws Exception {
		exception.expect(IllegalArgumentException.class);
		new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_ATTRIBUTE_XPATH, FilterType.PRUNE, -1, -1, indent);
	}
		
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_XPATH, FilterType.PRUNE, -1, -1, indent);
		Assert.assertNull(filter.process("</xml>"));
	}
	
	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_XPATH, FilterType.PRUNE, -1, -1, indent);
		Assert.assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
}
