package com.github.skjolber.xmlfilter.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.jupiter.api.Test;


public class XmlFilterFactoryTest extends BaseXmlFilterTest {

	@Test
	public void test_setters() throws Exception {
		DefaultXmlFilterFactory factory = new DefaultXmlFilterFactory();
		
		factory.setAnonymizeFilterList(Arrays.asList("/a/b"));
		factory.setPruneFilterList(Arrays.asList("/c/d"));

		assertThat(factory.getAnonymizeFilters(), is(new String[]{"/a/b"}));
		assertThat(factory.getPruneFilters(), is(new String[]{"/c/d"}));
	}

	
	
}
