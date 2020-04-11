package com.skjolberg.xmlfilter.w3c.dom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class W3cDomXPathXmlFilterTest extends BaseW3cDomXPathXmlIndentationFilterTest {

	@Test
	public void testFilter() throws Exception {
		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter filter = factory.getFilter(context, null, null);
		
		W3cDomXPathXmlFilter w3cDomXPathXmlIndentationFilter = new W3cDomXPathXmlFilter(false, true, filter);
	
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(w3cDomXPathXmlIndentationFilter);
		assertProcess(filters);
	}
	
	@Test
	public void filter_invalidXML_filtering() throws Exception {
		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter xpathFilter = factory.getFilter(context, null, null);
		
		W3cDomXPathXmlFilter filter = new W3cDomXPathXmlFilter(false, true, xpathFilter);
		
		assertNull(filter.process("</xml>"));
		assertNull(filter.process("</xml>".toCharArray()));
		assertFalse(filter.process("</xml>".toCharArray(), 0, 6, new StringBuilder()));
		assertFalse(filter.process(new StringReader("</xml>"), 6, new StringBuilder()));
	}
}	

