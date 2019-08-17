module com.github.skjolber.xmlfilter.core {
	requires com.github.skjolber.xmlfilter;
	requires com.github.skjolber.xmlfilter.base;
	requires com.github.skjolber.xmlfilter.core;
	
	exports com.github.skjolber.xmlfilter.soap.SoapHeaderXmlFilterFactory;
	
    provides com.github.skjolber.xmlfilter.XmlFilterFactory with com.github.skjolber.xmlfilter.soap.SoapHeaderXmlFilterFactory;
}