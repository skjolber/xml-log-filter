package com.github.skjolber.xmlfilter.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.xmlfilter.core.MaxNodeLengthXmlIndentationFilter;
import com.skjolberg.xmlfilter.XmlFilter;

public class MaxNodeLengthXmlIndentationFilterTest extends BaseXmlFilterTest {

	@Test
	public void filter_text_filtered() throws Exception {
		MaxNodeLengthXmlIndentationFilter xmlFilter = new MaxNodeLengthXmlIndentationFilter(true, -1, -1, indent);
		assertProcess(xmlFilter);
		assertValidXmlConformant(xmlFilter);
	}	

	@Test
	public void filter_maxlength_text() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MaxNodeLengthXmlIndentationFilter(true, DEFAULT_MAX_LENGTH, -1, indent));
		filters.add(new MaxNodeLengthXmlIndentationFilter(false, DEFAULT_MAX_LENGTH, -1, indent));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_maxlength_cdata() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MaxNodeLengthXmlIndentationFilter(true, -1, DEFAULT_MAX_LENGTH, indent));
		filters.add(new MaxNodeLengthXmlIndentationFilter(false, -1, DEFAULT_MAX_LENGTH, indent));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_maxlength() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MaxNodeLengthXmlIndentationFilter(true, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH, indent));
		filters.add(new MaxNodeLengthXmlIndentationFilter(false, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH, indent));
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new MaxNodeLengthXmlIndentationFilter(true, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH, indent);
		Assert.assertNull(filter.process("</xml>"));
	}
	
	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new MaxNodeLengthXmlIndentationFilter(true, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH, indent);
		Assert.assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
}
