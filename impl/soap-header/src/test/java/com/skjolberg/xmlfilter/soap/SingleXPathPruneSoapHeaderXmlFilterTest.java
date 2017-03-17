package com.skjolberg.xmlfilter.soap;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.skjolberg.xmlfilter.XmlFilter;

public class SingleXPathPruneSoapHeaderXmlFilterTest extends SingleXPathStAXXmlFilterTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void filter_text_filtered() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, -1));
			filters.add(new SingleXPathPruneSoapHeaderXmlFilter(false, xpath, -1));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}	

	@Test
	public void filter_passthrough_xpath() throws Exception {
		String[] passthroughXPaths = {PASSTHROUGH_XPATH};
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : passthroughXPaths) {
			filters.add(new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, 1));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_textWithAny_throwsException() throws Exception {
		exception.expect(IllegalArgumentException.class);
		
		new SingleXPathPruneSoapHeaderXmlFilter(true, DEFAULT_ANY_XPATH, 1);
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathPruneSoapHeaderXmlFilter(true, DEFAULT_XPATH, 1);
		Assert.assertNull(filter.process("</xml>"));
	}

	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathPruneSoapHeaderXmlFilter(true, DEFAULT_XPATH, 1);
		Assert.assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
	@Test
	public void filter_soap() throws IOException {
		String string = IOUtils.toString(getClass().getResourceAsStream("/soap/1k.xml"), StandardCharsets.UTF_8);
		
		String xpath = "/Envelope/Header/Security/UsernameToken";
		XmlFilter filter = new SingleXPathPruneSoapHeaderXmlFilter(true, xpath, 1);
		
		String process = filter.process(string);
		Assert.assertFalse(process.contains("Password"));
	}

}
