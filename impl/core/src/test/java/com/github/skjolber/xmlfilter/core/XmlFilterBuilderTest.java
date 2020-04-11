package com.github.skjolber.xmlfilter.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.github.skjolber.xmlfilter.XmlFilter;

public class XmlFilterBuilderTest {

	@Test
	public void testBuilderNone() throws Exception {
		XmlFilter newXmlFilter = XmlFilterBuilder.newXmlFilter().build();
		
		assertEquals(MaxNodeLengthXmlFilter.class, newXmlFilter.getClass());
	}
	
	@Test
	public void testBuilderAll() throws Exception {
		XmlFilter newXmlFilter = XmlFilterBuilder.newXmlFilter()
				.anonymize(new String[]{"/abcd/a"})
				.keepXMLDeclaration()
				.maxCDataNodeLength(1024)
				.maxNodeLength(1024)
				.maxTextNodeLength(1024)
				.prune(new String[]{"/abcd/a"})
				.build();
		
		assertNotNull(newXmlFilter);
	}
	
}