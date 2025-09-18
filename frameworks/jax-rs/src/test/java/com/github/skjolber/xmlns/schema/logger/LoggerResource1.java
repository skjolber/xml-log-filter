package com.github.skjolber.xmlns.schema.logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;

@Path("logger1")
public class LoggerResource1 {

	@Produces("application/xml")
	@Consumes("application/xml")
    @Path("performLogMessageObject")
    @POST
    @XmlLogFilter(anonymizeFilters = {"/performLogMessageRequest/address"}, maxCDATANodeLength = 1024, maxTextNodeLength = 1024)
    public PerformLogMessageResponse performLogMessageObject(PerformLogMessageRequest r) {
    	
    	PerformLogMessageResponse response = new PerformLogMessageResponse();
    	response.setStatus(1);
    	
        return response;
    }

	@Produces("text/plain")
	@Consumes("application/xml")
    @Path("performLogMessageRequestObject")
    @POST
    @XmlLogFilter
    public Integer performLogMessageRequestObject(PerformLogMessageRequest r) {
        return new Integer(1);
    }

	@Consumes({MediaType.TEXT_PLAIN, MediaType.TEXT_XML})
	@Produces("application/xml")
    @Path("performLogMessageResponseObject")
	@POST
    @XmlLogFilter
    public PerformLogMessageResponse performLogMessageResponseObject(String text) {
    	PerformLogMessageResponse response = new PerformLogMessageResponse();
    	response.setStatus(1);
    	
        return response;
    }

	@Consumes("application/xml")
    @Path("performLogMessageEmptyResponse")
    @POST
    @XmlLogFilter
    public void performLogMessageEmptyResponse(PerformLogMessageRequest r) {
    }
	
    @Path("performLogMessageParameter/{message}")
    @GET
    @XmlLogFilter
    public PerformLogMessageResponse performLogMessageParameter(@PathParam("message") String message) {
    	PerformLogMessageResponse response = new PerformLogMessageResponse();
    	response.setStatus(1);
    	
        return response;
    }
}