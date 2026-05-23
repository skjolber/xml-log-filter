module com.github.skjolber.xmlfilter.soap {
	requires com.github.skjolber.xmlfilter;
	requires com.github.skjolber.xmlfilter.base;
	requires com.github.skjolber.xmlfilter.core;
	
	exports com.skjolberg.xmlfilter.soap;
	
    provides com.github.skjolber.xmlfilter.XmlFilterFactory with com.skjolberg.xmlfilter.soap.SoapHeaderXmlFilterFactory;
}