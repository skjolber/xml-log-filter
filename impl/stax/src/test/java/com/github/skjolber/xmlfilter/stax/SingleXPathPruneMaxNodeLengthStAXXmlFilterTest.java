package com.github.skjolber.xmlfilter.stax;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class SingleXPathPruneMaxNodeLengthStAXXmlFilterTest extends SingleXPathStAXXmlFilterTest {
	/* TODO double max length
	@Test
	public void filter_xpath_maxlength() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));

		}
		assertProcess(filters);
	}
	*/

	@Test
	public void filter_text_filtered() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(false, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
	}	
	
	@Test
	public void filter_xpath_maxlength_cdata() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(false, xpath, 1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}

	@Test
	public void filter_xpath_maxlength_text() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}

	@Test
	public void filter_maxlength_text() throws Exception {
		String[] regularXPaths = {PASSTHROUGH_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}

	@Test
	public void filter_maxlength_cdata() throws Exception {
		String[] regularXPaths = {PASSTHROUGH_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(false, xpath, 1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}

	@Test
	public void filter_passthrough_xpath() throws Exception {
		String[] passthroughXPaths = {PASSTHROUGH_XPATH};
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : passthroughXPaths) {
			filters.add(new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
	}
	
	@Test
	public void filter_textWithAny_throwsException() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, DEFAULT_ANY_XPATH, -1, -1, xmlInputFactory, xmlOutputFactory);
		});
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, DEFAULT_XPATH, -1, -1, xmlInputFactory, xmlOutputFactory);
		assertNull(filter.process("</xml>"));
	}

	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathPruneMaxNodeLengthStAXXmlFilter(true, DEFAULT_XPATH, -1, -1, xmlInputFactory, xmlOutputFactory);
		assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
}
