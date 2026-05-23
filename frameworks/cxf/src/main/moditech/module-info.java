module com.github.skjolber.xmlfilter.cxf {
	requires com.github.skjolber.xmlfilter;
	requires com.github.skjolber.xmlfilter.base;
	requires com.github.skjolber.xmlfilter.soap;

	exports com.github.skjolber.xmlfilter.cxf;
	exports org.apache.cxf.ext.logging.osgi;
}
