package com.github.skjolber.xmlfilter.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class MaxNodeLengthXmlFilterTest extends BaseXmlFilterTest {

	@Test
	public void filter_maxlength_text() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MaxNodeLengthXmlFilter(true, DEFAULT_MAX_LENGTH, -1));
		filters.add(new MaxNodeLengthXmlFilter(false, DEFAULT_MAX_LENGTH, -1));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_maxlength_cdata() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MaxNodeLengthXmlFilter(true, -1, DEFAULT_MAX_LENGTH));
		filters.add(new MaxNodeLengthXmlFilter(false, -1, DEFAULT_MAX_LENGTH));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_passthrough() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MaxNodeLengthXmlFilter(true, -1, -1));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new MaxNodeLengthXmlFilter(true, -1, 127);
		Assert.assertNull(filter.process("</xml>"));
	}
	
	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new MaxNodeLengthXmlFilter(true, -1, 127);
		Assert.assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
}
