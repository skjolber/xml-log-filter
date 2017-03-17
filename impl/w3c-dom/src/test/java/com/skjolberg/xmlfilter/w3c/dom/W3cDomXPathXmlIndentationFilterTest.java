package com.skjolberg.xmlfilter.w3c.dom;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.skjolberg.xmlfilter.XmlFilter;
import com.skjolberg.xmlfilter.w3c.dom.MapNamespaceContext;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilter;
import com.skjolberg.xmlfilter.w3c.dom.XPathFilterFactory;

public class W3cDomXPathXmlIndentationFilterTest extends BaseW3cDomXPathXmlIndentationFilterTest {

	@Test
	public void testFilter() throws Exception {
		XPathFilterFactory factory = new XPathFilterFactory();
		Map<String, String> namespaces = new HashMap<String, String>();
		MapNamespaceContext context = new MapNamespaceContext(namespaces);
		XPathFilter filter = factory.getFilter(context, null, null);
		
		W3cDomXPathXmlIndentationFilter w3cDomXPathXmlIndentationFilter = new W3cDomXPathXmlIndentationFilter(false, true, filter);
	
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
		
		W3cDomXPathXmlIndentationFilter filter = new W3cDomXPathXmlIndentationFilter(false, true, xpathFilter);
		
		Assert.assertNull(filter.process("</xml>"));
		Assert.assertNull(filter.process("</xml>".toCharArray()));
		Assert.assertFalse(filter.process("</xml>".toCharArray(), 0, 6, new StringBuilder()));
		Assert.assertFalse(filter.process(new StringReader("</xml>"), 6, new StringBuilder()));
	}
}	

