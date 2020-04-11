package com.github.skjolber.xmlfilter.core;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

import static org.junit.jupiter.api.Assertions.*;

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
		assertNotNull(filter.process("</xml>"));
		assertNotNull(filter.process("</xml>".toCharArray()));
		assertTrue(filter.process("</xml>".toCharArray(), 0, 6, new StringBuilder()));
		assertNotNull(filter.process(new StringReader("</xml>"), 6, new StringBuilder()));
	}
}
