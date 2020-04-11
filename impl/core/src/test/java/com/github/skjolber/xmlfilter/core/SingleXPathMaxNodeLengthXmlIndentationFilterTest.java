package com.github.skjolber.xmlfilter.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.filter.AbstractXPathFilter.FilterType;

public class SingleXPathMaxNodeLengthXmlIndentationFilterTest extends BaseXmlFilterTest {

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
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_ANY_XPATH, FilterType.PRUNE, -1, -1, indent);
		});
	}
	
	@Test
	public void filter_pruneTextWithAttribute_throwsException() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_ATTRIBUTE_XPATH, FilterType.PRUNE, -1, -1, indent);
		});
	}
		
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_XPATH, FilterType.PRUNE, -1, -1, indent);
		assertNull(filter.process("</xml>"));
	}
	
	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathMaxNodeLengthXmlIndentationFilter(true, DEFAULT_XPATH, FilterType.PRUNE, -1, -1, indent);
		assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
}
