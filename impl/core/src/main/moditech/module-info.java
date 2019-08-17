module com.github.skjolber.xmlfilter.core {
	requires com.github.skjolber.xmlfilter;
	requires com.github.skjolber.xmlfilter.base;
	
	exports com.github.skjolber.xmlfilter.core.DefaultXmlFilterFactory;
	exports com.github.skjolber.xmlfilter.core.XmlFilterBuilder;
	
    provides com.github.skjolber.xmlfilter.XmlFilterFactory with com.github.skjolber.xmlfilter.core.DefaultXmlFilterFactory;
}