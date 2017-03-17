package com.github.skjolber.xmlfilter.stax.soap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.skjolber.xmlfilter.stax.soap.SingleXPathAnonymizeStAXSoapHeaderXmlFilter;
import com.skjolberg.xmlfilter.XmlFilter;

public class SingleXPathAnonymizeStAXSoapHeaderXmlFilterTest extends BaseStAXSoapHeaderXmlFilterTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	/* TODO double max length
	@Test
	public void filter_xpath_maxlength() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));
			filters.add(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(false, xpath, DEFAULT_MAX_LENGTH, DEFAULT_MAX_LENGTH));

		}
		assertProcess(filters);
	}
	*/

	@Test
	public void filter_text_filtered() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, -1, xmlInputFactory, xmlOutputFactory));
			filters.add(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(false, xpath, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
	}	

	@Test
	public void filter_passthrough_xpath() throws Exception {
		String[] passthroughXPaths = {PASSTHROUGH_XPATH};
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : passthroughXPaths) {
			filters.add(new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, -1, xmlInputFactory, xmlOutputFactory));
		}
		assertProcess(filters);
	}
	
	@Test
	public void filter_textWithAny_throwsException() throws Exception {
		exception.expect(IllegalArgumentException.class);
		
		new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, DEFAULT_ANY_XPATH, -1, xmlInputFactory, xmlOutputFactory);
	}
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, DEFAULT_XPATH, -1, xmlInputFactory, xmlOutputFactory);
		Assert.assertNull(filter.process("</xml>"));
	}

	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, DEFAULT_XPATH, -1, xmlInputFactory, xmlOutputFactory);
		Assert.assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	@Test
	public void testSoap1k() throws IOException {
		String string = IOUtils.toString(getClass().getResourceAsStream("/soap/1k.xml"), StandardCharsets.UTF_8);
		
		String xpath = "/Envelope/Header/Security/UsernameToken/Password";
		XmlFilter filter = new SingleXPathAnonymizeStAXSoapHeaderXmlFilter(true, xpath, 1, xmlInputFactory, xmlOutputFactory);
		
		System.out.println(filter.process(string));

		
	}
}
