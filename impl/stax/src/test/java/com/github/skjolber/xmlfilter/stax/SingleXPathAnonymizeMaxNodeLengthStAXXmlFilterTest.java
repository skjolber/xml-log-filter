package com.github.skjolber.xmlfilter.stax;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.skjolber.xmlfilter.stax.SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter;
import com.skjolberg.xmlfilter.XmlFilter;

public class SingleXPathAnonymizeMaxNodeLengthStAXXmlFilterTest extends SingleXPathStAXXmlFilterTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	/* TODO double max length
	@Test
	public void filter_xpath_maxlength() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));

		}
		assertProcess(filters);
	}
	*/

	@Test
	public void filter_text_filtered() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(false, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
	}	
	
	@Test
	public void filter_xpath_maxlength_cdata() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(false, xpath, 1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}

	@Test
	public void filter_xpath_maxlength_text() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}

	@Test
	public void filter_maxlength_text() throws Exception {
		String[] regularXPaths = {PASSTHROUGH_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}

	@Test
	public void filter_maxlength_cdata() throws Exception {
		String[] regularXPaths = {PASSTHROUGH_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(false, xpath, 1, DEFAULT_MAX_LENGTH, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}


	@Test
	public void filter_passthrough_xpath() throws Exception {
		String[] passthroughXPaths = {PASSTHROUGH_XPATH};
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : passthroughXPaths) {
			filters.add(new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, xpath, -1, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
		
	}
	
	@Test
	public void filter_textWithAny_throwsException() throws Exception {
		exception.expect(IllegalArgumentException.class);
		
		new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, DEFAULT_ANY_XPATH, -1, -1, xmlInputFactory, xmlOutputFactory);
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, DEFAULT_XPATH, -1, -1, xmlInputFactory, xmlOutputFactory);
		Assert.assertNull(filter.process("</xml>"));
	}

	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter(true, DEFAULT_XPATH, -1, -1, xmlInputFactory, xmlOutputFactory);
		Assert.assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
}
