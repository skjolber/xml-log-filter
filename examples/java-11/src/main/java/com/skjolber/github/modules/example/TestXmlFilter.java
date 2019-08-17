package com.skjolber.github.modules.example;

import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.core.XmlFilterBuilder;

public class TestXmlFilter {

	public static final void main(String[] args) {
		
		XmlFilter xmlFilter = XmlFilterBuilder.newXmlFilter()
				.anonymize(new String[]{"/abcd/a"})
				.keepXMLDeclaration()
				.maxCDataNodeLength(1024)
				.maxNodeLength(1024)
				.maxTextNodeLength(1024)
				.prune(new String[]{"/abcd/a"})
				.build();
		
		System.out.println("Got filter " + xmlFilter.getClass().getName());
	}
}
