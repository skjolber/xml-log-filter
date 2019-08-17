package com.github.skjolber.xmlfilter.core;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class DefaultXmlFilterTest extends BaseXmlFilterTest {

	@Test
	public void filter() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new DefaultXmlFilter());
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_invalidXML_filtering() throws Exception {
		XmlFilter filter = new DefaultXmlFilter();
		Assert.assertNotNull(filter.process("</xml>"));
		Assert.assertNotNull(filter.process("</xml>".toCharArray()));
		Assert.assertTrue(filter.process("</xml>".toCharArray(), 0, 6, new StringBuilder()));
		Assert.assertNotNull(filter.process(new StringReader("</xml>"), 6, new StringBuilder()));
	}
}
