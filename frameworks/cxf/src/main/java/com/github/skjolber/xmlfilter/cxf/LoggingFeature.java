/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.skjolber.xmlfilter.cxf;

import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.annotations.Provider.Type;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

import com.github.skjolber.indent.LinebreakType;
import com.skjolberg.xmlfilter.XmlFilter;
import com.skjolberg.xmlfilter.filter.AbstractXmlFilterFactory;
import com.skjolberg.xmlfilter.soap.SoapHeaderXmlFilterFactory;

/**
 * This class is used to control message-on-the-wire logging. 
 * By attaching this feature to an endpoint, you
 * can specify logging. If this feature is present, an endpoint will log input
 * and output of ordinary and log messages.
 * <pre>
 * <![CDATA[
    <jaxws:endpoint ...>
      <jaxws:features>
       <bean class="org.apache.cxf.ext.logging.LoggingFeature"/>
      </jaxws:features>
    </jaxws:endpoint>
  ]]>
  </pre>
 */
@NoJSR250Annotations
@Provider(value = Type.Feature)
public class LoggingFeature extends AbstractFeature {
	
    private LogEventSender sender;
    private LoggingInInterceptor in;
    private LoggingOutInterceptor out;
    
    protected AbstractXmlFilterFactory factory = new SoapHeaderXmlFilterFactory();

    protected int logThreshold = -1; // maximum payload logged (after filtering, in characters)
    protected int discThreshold = -1; // maximum payload held on disc (before filtering, in bytes)
    protected int memThreshold = -1; // maximum payload held in memory (before filtering, in bytes)
    
    protected boolean logBinary;
    protected boolean logMultipart = true;

    protected XmlFilter xmlFilter;
    protected LoggingAccumulatorInteceptor accumulator;
    
    public LoggingFeature(LogEventSender sender) {
        this.sender = sender;
		
		accumulator = new LoggingAccumulatorInteceptor();
    }

    public LoggingFeature() {
    	this(new Slf4jEventSender());
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
    	
    	if(xmlFilter == null) {
	    	xmlFilter = factory.newXmlFilter();
    	}
    	
    	if(in == null) {
    		in = new LoggingInInterceptor(sender);
    		
            in.setDiscThreshold(discThreshold);
            in.setInMemThreshold(memThreshold);
            in.setLogThreshold(logThreshold);

            in.setLogBinary(logBinary);
            in.setLogMultipart(logMultipart);
            
        	in.setXmlFilter(xmlFilter);
    	}
    	if(out == null) {
    		out = new LoggingOutInterceptor(sender);
    		
            out.setDiscThreshold(discThreshold);
            out.setInMemThreshold(memThreshold);
            out.setLogThreshold(logThreshold);
            
            out.setLogBinary(logBinary);
            out.setLogMultipart(logMultipart);
            
        	out.setXmlFilter(xmlFilter);
    	}

        provider.getInInterceptors().add(accumulator);
        provider.getInInterceptors().add(in);
        provider.getInFaultInterceptors().add(in);
        
        provider.getOutInterceptors().add(out);
        provider.getOutFaultInterceptors().add(out);
    }

    public void setInMemThreshold(int inMemThreshold) {
    	if(in != null) {
	        in.setInMemThreshold(inMemThreshold);
	        out.setInMemThreshold(inMemThreshold);
    	}
    	accumulator.setInMemThreshold(inMemThreshold);
    }
    
    public void setSender(LogEventSender logEventSender) {
        this.sender = logEventSender;

        if(in != null) {
	        in.setLogEventSender(logEventSender);
	        out.setLogEventSender(logEventSender);
        }
    }

    /**
     * Log binary content?
     * @param logBinary defaults to false 
     */
    public void setLogBinary(boolean logBinary) {
    	this.logBinary = logBinary;
    	
    	if(in != null) {
    		in.setLogBinary(logBinary);
            out.setLogBinary(logBinary);
    	}
    	accumulator.setLogBinary(logBinary);
    }
    
    /**
     * Log multipart content? 
     * @param logMultipart defaults to true
     */
    
    public void setLogMultipart(boolean logMultipart) {
    	this.logMultipart = logMultipart;
    	
    	if(in != null) {
	        in.setLogMultipart(logMultipart);
	        out.setLogMultipart(logMultipart);
    	}
    	accumulator.setLogMultipart(logMultipart);
    }
    
    public void setDiscThreshold(int discThreshold) {
    	this.discThreshold = discThreshold;
    	
    	if(in != null) {
	        in.setDiscThreshold(discThreshold);
	        out.setDiscThreshold(discThreshold);
    	}
    	accumulator.setDiscThreshold(discThreshold);

	}
    
    public void setLogThreshold(int logThreshold) {
    	this.logThreshold = logThreshold;
    	if(in != null) {
	        in.setLogThreshold(logThreshold);
	        out.setLogThreshold(logThreshold);
    	}
	}

	public void setXmlDeclaration(boolean xmlDeclaration) {
		factory.setXmlDeclaration(xmlDeclaration);
		
		refreshFilter();
	}

	private void refreshFilter() {
		if(xmlFilter != null) {
			xmlFilter = factory.newXmlFilter();
			
        	in.setXmlFilter(xmlFilter);
        	out.setXmlFilter(xmlFilter);
		}
	}

	public void setMaxTextNodeLength(int maxTextNodeLength) {
		factory.setMaxTextNodeLength(maxTextNodeLength);
		refreshFilter();
	}

	public int getMaxTextNodeLength() {
		return factory.getMaxTextNodeLength();
	}

	public void setMaxCDATANodeLength(int maxCDATANodeLength) {
		factory.setMaxCDATANodeLength(maxCDATANodeLength);
		refreshFilter();
	}

	public int getMaxCDATANodeLength() {
		return factory.getMaxCDATANodeLength();
	}

	public boolean isXmlDeclaration() {
		return factory.isXmlDeclaration();
	}

	public void setPruneFilters(String... filters) {
		factory.setPruneFilters(filters);
		refreshFilter();
	}

	public void setPruneFilterList(List<String> filters) {
		factory.setPruneFilterList(filters);
		refreshFilter();
	}

	public String[] getPruneFilters() {
		return factory.getPruneFilters();
	}

	public void setAnonymizeFilters(String... filters) {
		factory.setAnonymizeFilters(filters);
		refreshFilter();
	}

	public void setAnonymizeFilterList(List<String> filters) {
		factory.setAnonymizeFilterList(filters);
		refreshFilter();
	}

	public String[] getAnonymizeFilters() {
		return factory.getAnonymizeFilters();
	}

	public boolean isIgnoreWhitespace() {
		return factory.isIgnoreWhitespace();
	}

	public void setIndent(boolean indent) {
		factory.setIndent(indent);
		refreshFilter();
	}

	public boolean isIndent() {
		return factory.isIndent();
	}

	public Character getIndentCharacter() {
		return factory.getIndentCharacter();
	}

	public void setIndentCharacter(Character indentCharacter) {
		factory.setIndentCharacter(indentCharacter);
		refreshFilter();
	}

	public Integer getIndentCount() {
		return factory.getIndentCount();
	}

	public void setIndentCount(Integer indentCount) {
		factory.setIndentCount(indentCount);
		refreshFilter();
	}

	public Integer getIndentPreparedLevels() {
		return factory.getIndentPreparedLevels();
	}

	public void setIndentPreparedLevels(Integer indentPreparedLevels) {
		factory.setIndentPreparedLevels(indentPreparedLevels);
		refreshFilter();
	}

	public Integer getIndentResetLevel() {
		return factory.getIndentResetLevel();
	}

	public void setIndentResetLevel(Integer indentResetLevel) {
		factory.setIndentResetLevel(indentResetLevel);
		refreshFilter();
	}

	public LinebreakType getIndentLinebreakType() {
		return factory.getIndentLinebreakType();
	}

	public void setIndentLinebreakType(LinebreakType indentLinebreakType) {
		factory.setIndentLinebreakType(indentLinebreakType);
		refreshFilter();
	}

	public void setIgnoreWhitespace(boolean ignoreWhitespace) {
		factory.setIgnoreWhitespace(ignoreWhitespace);
		refreshFilter();
	}

	public boolean isPropertySupported(String name) {
		return factory.isPropertySupported(name);
	}

	public int getMaxFilterMatches() {
		return factory.getMaxFilterMatches();
	}

	public void setMaxFilterMatches(int maxFilterMatches) {
		factory.setMaxFilterMatches(maxFilterMatches);
		refreshFilter();
	}
    
	public void setFactory(AbstractXmlFilterFactory factory) {
		this.factory = factory;
		refreshFilter();
	}
	
	public AbstractXmlFilterFactory getFactory() {
		return factory;
	}
}
