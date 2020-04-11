package com.github.skjolber.xmlfilter.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class SingleXPathAnonymizeMaxNodeLengthXmlFilterTest extends BaseXmlFilterTest {

	/* TODO double max length
	@Test
	public void filter_xpath_maxlength() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));

		}
		assertProcess(filters);
	}
	*/

	@Test
	public void filter_text_filtered() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, -1));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(false, xpath, -1, -1));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}	
	
	@Test
	public void filter_xpath_maxlength_cdata() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, DEFAULT_MAX_LENGTH));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(false, xpath, 1, DEFAULT_MAX_LENGTH));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_xpath_maxlength_text() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, -1));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, -1));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_maxlength_text() throws Exception {
		String[] regularXPaths = {PASSTHROUGH_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, -1));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, -1));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_maxlength_cdata() throws Exception {
		String[] regularXPaths = {PASSTHROUGH_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, DEFAULT_MAX_LENGTH));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(false, xpath, 1, DEFAULT_MAX_LENGTH));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}


	@Test
	public void filter_passthrough_xpath() throws Exception {
		String[] passthroughXPaths = {PASSTHROUGH_XPATH};
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : passthroughXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, xpath, -1, -1));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_textWithAny_throwsException() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, DEFAULT_ANY_XPATH, -1, -1);
		});
		
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, DEFAULT_XPATH, -1, -1);
		assertNull(filter.process("</xml>"));
	}

	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeMaxNodeLengthXmlFilter(true, DEFAULT_XPATH, -1, -1);
		assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
}
