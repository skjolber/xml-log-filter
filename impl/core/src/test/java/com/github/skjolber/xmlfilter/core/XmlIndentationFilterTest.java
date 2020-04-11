package com.github.skjolber.xmlfilter.core;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class XmlIndentationFilterTest extends BaseXmlFilterTest {

	@Test
	public void filter_text_filtered() throws Exception {
		assertProcess(new XmlIndentationFilter(true, indent));
		assertProcess(new XmlIndentationFilter(false, indent));
	}	
	
	@Test
    public void testConformance() throws Exception {
    	assertValidXmlConformant(new XmlIndentationFilter(true, indent));
    	assertValidXmlConformant(new XmlIndentationFilter(false, indent));
    }

	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new XmlIndentationFilter(true, indent);
		assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
}
