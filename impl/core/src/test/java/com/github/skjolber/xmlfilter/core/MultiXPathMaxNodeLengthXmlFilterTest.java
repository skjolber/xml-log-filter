package com.github.skjolber.xmlfilter.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.skjolber.xmlfilter.core.MultiXPathMaxNodeLengthXmlFilter;
import com.skjolberg.xmlfilter.XmlFilter;

public class MultiXPathMaxNodeLengthXmlFilterTest extends BaseXmlFilterTest {
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void passthrough_success() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, new String[]{PASSTHROUGH_XPATH}, new String[]{PASSTHROUGH_XPATH}));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_anon1() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, new String[]{xpath}, null));
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, new String[]{xpath}, null));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_anon2() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, null));
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, null));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}

	@Test
	public void filter_prune1() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, null, new String[]{xpath}));
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, null, new String[]{xpath}));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_prune2() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, null, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}));
			filters.add(new MultiXPathMaxNodeLengthXmlFilter(false, -1, -1, null, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
		
	
	@Test
	public void filter_maxlength_cdata() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MultiXPathMaxNodeLengthXmlFilter(true, -1, DEFAULT_MAX_LENGTH, null, null));
		filters.add(new MultiXPathMaxNodeLengthXmlFilter(false, -1, DEFAULT_MAX_LENGTH, null, null));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_maxlength_text() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MultiXPathMaxNodeLengthXmlFilter(true, DEFAULT_MAX_LENGTH, -1, null, null));
		filters.add(new MultiXPathMaxNodeLengthXmlFilter(false, DEFAULT_MAX_LENGTH, -1, null, null));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_pruneTextWithAttribute_throwsException() throws Exception {
		exception.expect(IllegalArgumentException.class);
		new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, null, new String[]{DEFAULT_ATTRIBUTE_XPATH});
	}
	
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, new String[]{DEFAULT_ATTRIBUTE_WILDCARD_XPATH}, null);
		Assert.assertNull(filter.process("</xml>"));
	}
	
	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, new String[]{DEFAULT_ATTRIBUTE_WILDCARD_XPATH}, null);
		Assert.assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}

	
	
}
