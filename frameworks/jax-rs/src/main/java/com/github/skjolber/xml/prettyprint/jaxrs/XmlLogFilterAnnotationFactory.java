package com.github.skjolber.xml.prettyprint.jaxrs;

import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.core.DefaultXmlFilterFactory;

public class XmlLogFilterAnnotationFactory {

	private static final XmlFilter defaultXmlFilter;
	static {
		DefaultXmlFilterFactory factory = new DefaultXmlFilterFactory();

		defaultXmlFilter = factory.newXmlFilter();
	}

	public XmlFilter getXmlFilter(XmlLogFilter annotation) {
		if(annotation == null) {
			return defaultXmlFilter;
		}

		boolean xmlDeclaration = annotation.xmlDeclaration();
		boolean indent = annotation.indent();

		int maxTextNodeLength = annotation.maxTextNodeLength();
		int maxCDATANodeLength = annotation.maxCDATANodeLength();

		String[] anonymizeFilters = annotation.anonymizeFilters();
		String[] pruneFilters = annotation.pruneFilters();

		XmlFilter xmlFilter;
		if(
				indent ||
				xmlDeclaration ||
				maxTextNodeLength != -1 ||
				maxCDATANodeLength != -1 ||
				(anonymizeFilters != null && anonymizeFilters.length > 0) ||
				(pruneFilters != null && pruneFilters.length > 0)
				) {
			DefaultXmlFilterFactory factory = new DefaultXmlFilterFactory();
			factory.setXmlDeclaration(xmlDeclaration);
			factory.setIndent(indent);

			factory.setMaxTextNodeLength(maxTextNodeLength);
			factory.setMaxCDATANodeLength(maxCDATANodeLength);

			factory.setAnonymizeFilters(anonymizeFilters);
			factory.setPruneFilters(pruneFilters);

			xmlFilter = factory.newXmlFilter();
		} else {
			xmlFilter = defaultXmlFilter;
		}
		return xmlFilter;
	}

}
