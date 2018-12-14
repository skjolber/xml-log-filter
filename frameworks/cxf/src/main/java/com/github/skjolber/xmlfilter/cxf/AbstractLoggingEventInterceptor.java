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

import java.util.UUID;

import org.apache.cxf.ext.logging.event.DefaultLogEventMapper;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;

import com.skjolberg.xmlfilter.XmlFilter;

public abstract class AbstractLoggingEventInterceptor extends AbstractLoggingInterceptor {
	
    protected LogEventSender logEventSender;
    protected DefaultLogEventMapper logEventMapper = new DefaultLogEventMapper();
    
	protected XmlFilter xmlFilter;

    public AbstractLoggingEventInterceptor(String phase, LogEventSender sender) {
        super(phase);
        this.logEventSender = sender;
    }
    
    public void createExchangeId(Message message) {
        Exchange exchange = message.getExchange();
        String exchangeId = (String)exchange.get(LogEvent.KEY_EXCHANGE_ID);
        if (exchangeId == null) {
            exchangeId = UUID.randomUUID().toString();
            exchange.put(LogEvent.KEY_EXCHANGE_ID, exchangeId);
        }
    }
    
    public void setXmlFilter(XmlFilter xmlFilter) {
		this.xmlFilter = xmlFilter;
	}
 
    public void setLogEventMapper(DefaultLogEventMapper logEventMapper) {
		this.logEventMapper = logEventMapper;
	}
    
    public void setLogEventSender(LogEventSender logEventSender) {
		this.logEventSender = logEventSender;
	}
    
	protected void filterPayload(final LogEvent event, String payload) {
		String contentType = event.getContentType();
		
        String filtered;
    	if (xmlFilter != null 
    			&& (contentType != null 
    			&& contentType.indexOf("xml") >= 0 
    			&& contentType.toLowerCase().indexOf("multipart/related") < 0) 
    			&& payload.length() > 0) {		
	        if(event.isTruncated()) {
	        	// stream filtering?
	            filtered = xmlFilter.process(payload);
	        } else {
	            filtered = xmlFilter.process(payload);
	        }
	        if(filtered == null) {
	        	filtered = payload;
	        }
        } else {
        	filtered = payload;
        }
        
        if(filtered == null) {
        	filtered = payload;
        }
        
        // apply log limit
        if(logThreshold != -1 && filtered.length() > logThreshold) {
        	filtered = filtered.substring(0, logThreshold);
        	
        	event.setTruncated(true);
        }
        
        event.setPayload(filtered);
	}
	
}
