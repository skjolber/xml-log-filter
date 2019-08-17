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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedWriter;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import com.github.skjolber.xmlfilter.XmlFilter;

/**
 * 
 */
@NoJSR250Annotations
public class LoggingInInterceptor extends AbstractLoggingEventInterceptor {

    public LoggingInInterceptor() {
    	this(new Slf4jEventSender());
    }

    public LoggingInInterceptor(LogEventSender sender) {
        super(Phase.PRE_INVOKE, sender);
    }

    public void handleMessage(Message message) throws Fault {
        createExchangeId(message);
        final LogEvent event = logEventMapper.map(message);
        if (shouldLogContent(event)) {
            addContent(message, event);
        } else {
            event.setPayload(AbstractLoggingInterceptor.CONTENT_SUPPRESSED);
        }
        logEventSender.send(event);
        
    }

    private void addContent(Message message, final LogEvent event) {
        try {
            CachedOutputStream cos = message.getContent(CachedOutputStream.class);
            
            if (cos != null) {
                handleOutputStream(event, message, cos);
            } else {
                CachedWriter writer = message.getContent(CachedWriter.class);
                if (writer != null) {
                    handleWriter(event, writer);
                } else {
                }
            }
        } catch (IOException e) {
            throw new Fault(e);
        }
    }

    private void handleOutputStream(final LogEvent event, Message message, CachedOutputStream cos) throws IOException {
        event.setFullContentFile(cos.getTempFile());
        
        String encoding = (String)message.get(Message.ENCODING);
        if (StringUtils.isEmpty(encoding)) {
            encoding = StandardCharsets.UTF_8.name();
        }

        StringBuilder payload;
        if(discThreshold != -1 && cos.size() > discThreshold) {
            payload = new StringBuilder(discThreshold);
            event.setTruncated(true);
        } else {
            payload = new StringBuilder((int)cos.size());
        }

        cos.writeCacheTo(payload, encoding, payload.capacity());
        cos.close();

        filterPayload(event, payload.toString());
    }

    private void handleWriter(final LogEvent event, CachedWriter writer) throws IOException {
        event.setFullContentFile(writer.getTempFile());
        
        StringBuilder payload;
        if(discThreshold != -1 && writer.size() > discThreshold) {
            payload = new StringBuilder(discThreshold);
            
            event.setTruncated(true);
        } else {
            payload = new StringBuilder((int)writer.size());
        }

        writer.writeCacheTo(payload, payload.capacity());
        
        filterPayload(event, payload.toString());
    }

    public void setXmlFilter(XmlFilter xmlFilter) {
		this.xmlFilter = xmlFilter;
	}
}
