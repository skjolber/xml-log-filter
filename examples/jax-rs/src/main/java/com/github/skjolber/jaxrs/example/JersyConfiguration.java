package com.github.skjolber.jaxrs.example;

import jakarta.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import com.github.skjolber.jaxrs.example.resource.MyResource;
import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;

@Configuration
@ApplicationPath("/rest")
public class JersyConfiguration extends ResourceConfig {

	public JersyConfiguration() {
		packages(MyResource.class.getPackage().getName(), XmlLogFilter.class.getPackage().getName());
	}
	
}
