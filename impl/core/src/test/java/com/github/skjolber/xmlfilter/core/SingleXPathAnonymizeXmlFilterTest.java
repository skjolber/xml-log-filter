package com.github.skjolber.xmlfilter.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class SingleXPathAnonymizeXmlFilterTest extends BaseXmlFilterTest {

	@Test
	public void filter_text_filtered() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeXmlFilter(true, xpath));
			filters.add(new SingleXPathAnonymizeXmlFilter(false, xpath));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_passthrough() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new SingleXPathAnonymizeXmlFilter(true, PASSTHROUGH_XPATH));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_textWithAny_throwsException() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new SingleXPathAnonymizeXmlFilter(true, DEFAULT_ANY_XPATH);
		});
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeXmlFilter(true, DEFAULT_XPATH);
		assertNull(filter.process("</xml>"));
	}
	
	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeXmlFilter(true, DEFAULT_XPATH);
		assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}	
}
