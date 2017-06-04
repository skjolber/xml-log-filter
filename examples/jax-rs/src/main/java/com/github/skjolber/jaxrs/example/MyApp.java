package com.github.skjolber.jaxrs.example;

import org.glassfish.jersey.server.ResourceConfig;

import com.github.skjolber.jaxrs.example.resource.MyResource;
import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;
import com.skjolberg.xmlfilter.XmlFilter;

public class MyApp extends ResourceConfig {

	public MyApp() {
		packages(MyResource.class.getPackage().getName(), XmlLogFilter.class.getPackage().getName());
	}
	
}
