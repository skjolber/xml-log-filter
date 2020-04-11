package com.github.skjolber.xmlfilter.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.github.skjolber.xmlfilter.XmlFilter;


public class MultiXPathXmlFilterTest extends BaseXmlFilterTest {

	@Test
	public void passthrough_success() throws Exception {
		List<XmlFilter> filters = new ArrayList<>();
		filters.add(new MultiXPathXmlFilter(true, new String[]{PASSTHROUGH_XPATH}, new String[]{PASSTHROUGH_XPATH}));
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_anon1() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlFilter(true, new String[]{xpath}, null));
			filters.add(new MultiXPathXmlFilter(false, new String[]{xpath}, null));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_anon2() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH, DEFAULT_ATTRIBUTE_XPATH, DEFAULT_ATTRIBUTE_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlFilter(true, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, null));
			filters.add(new MultiXPathXmlFilter(false, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}, null));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	
	@Test
	public void filter_prune1() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlFilter(true, null, new String[]{xpath}));
			filters.add(new MultiXPathXmlFilter(false, null, new String[]{xpath}));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
	
	@Test
	public void filter_prune2() throws Exception {
		String[] regularXPaths = {DEFAULT_XPATH, DEFAULT_WILDCARD_XPATH};
		
		List<XmlFilter> filters = new ArrayList<>();
		for(String xpath : regularXPaths) {
			filters.add(new MultiXPathXmlFilter(true, null, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}));
			filters.add(new MultiXPathXmlFilter(false, null, new String[]{PASSTHROUGH_XPATH, xpath, PASSTHROUGH_XPATH}));
		}
		assertProcess(filters);
		assertValidXmlConformant(filters);
	}
		
	
	@Test
	public void filter_pruneTextWithAttribute_throwsException() throws Exception {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new MultiXPathMaxNodeLengthXmlFilter(true, -1, -1, null, new String[]{DEFAULT_ATTRIBUTE_XPATH});
		});
	}
	
	
	@Test
	public void filter_invalidXML_noFiltering() throws Exception {
		XmlFilter filter = new MultiXPathXmlFilter(true, new String[]{DEFAULT_ATTRIBUTE_WILDCARD_XPATH}, null);
		assertNull(filter.process("</xml>"));
	}
	
	@Test
	public void filter_invalidRange_noFiltering() throws Exception {
		XmlFilter filter = new MultiXPathXmlFilter(true, new String[]{DEFAULT_ATTRIBUTE_WILDCARD_XPATH}, null);
		assertFalse(filter.process("<xml></xml>".toCharArray(), 0, 100, new StringBuilder()));
	}
	
	
}
