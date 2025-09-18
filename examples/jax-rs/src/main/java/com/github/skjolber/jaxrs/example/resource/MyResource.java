package com.github.skjolber.jaxrs.example.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;

/**
 * Resource (exposed at "myResource" path)
 */
@Component
@Path("/myResource")
public class MyResource {

	private static final String resourceValue = "<a><b>Value</b></a>";
	
	public MyResource() {
	}
	
    @GET
    @Produces(MediaType.TEXT_XML)
    @XmlLogFilter
    @Path("/myMethod")
    public String myMethod() {
        return resourceValue;
    }
    
    @GET
    @Produces(MediaType.TEXT_XML)
    @XmlLogFilter(anonymizeFilters = {"/a/b"})
    @Path("/myFilterMethod")
    public String myFilerMethod() {
        return resourceValue;
    }
    
}
