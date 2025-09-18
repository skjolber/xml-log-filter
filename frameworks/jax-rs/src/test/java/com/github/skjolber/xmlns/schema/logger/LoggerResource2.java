package com.github.skjolber.xmlns.schema.logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.github.skjolber.xml.prettyprint.jaxrs.XmlLogFilter;

@Path("logger2")
@XmlLogFilter(anonymizeFilters = {"/*/address"})
public class LoggerResource2 {

	@Produces("application/xml")
	@Consumes("application/xml")
    @Path("performLogMessageObject")
    @POST
    public PerformLogMessageResponse performLogMessageObject(PerformLogMessageRequest r) {
    	
    	PerformLogMessageResponse response = new PerformLogMessageResponse();
    	response.setStatus(1);
    	
        return response;
    }

}