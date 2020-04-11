package com.github.skjolber.xmlfilter.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class MultiXPathXmlIndentationFilterTest extends BaseXmlFilterTest {

	@Test
	public void filter_anon1() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlIndentationFilter(true, new String[]{xpath}, null, indent));
			filters.add(new MultiXPathXmlIndentationFilter(false, new String[]{xpath}, null, indent));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_anon2() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlIndentationFilter(true, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, null, indent));
			filters.add(new MultiXPathXmlIndentationFilter(false, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, null, indent));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_prune1() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlIndentationFilter(true, null, new String[]{xpath}, indent));
			filters.add(new MultiXPathXmlIndentationFilter(false, null, new String[]{xpath}, indent));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_prune2() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlIndentationFilter(true, null, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, indent));
			filters.add(new MultiXPathXmlIndentationFilter(false, null, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, indent));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_pruneTextWithAttribute_throwsException() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, null, new String[]{DEFAULT_ATTRIBUTE_XPATH});
		});
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new MultiXPathXmlIndentationFilter(true, new String[]{DEFAULT_ATTRIBUTE_WILDCARD_XPATH}, null, indent);
		assertNull(filter.process("</xml>"));
	}

	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new MultiXPathXmlIndentationFilter(true, new String[]{DEFAULT_ATTRIBUTE_WILDCARD_XPATH}, null, indent);
		assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}

}
