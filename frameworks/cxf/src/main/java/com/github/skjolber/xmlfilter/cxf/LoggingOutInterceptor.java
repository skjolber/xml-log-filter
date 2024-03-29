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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * 
 */
@NoJSR250Annotations
public class LoggingOutInterceptor extends AbstractLoggingEventInterceptor {

	private Set<String> sensitiveProtocolHeaders = new HashSet<>();

    public LoggingOutInterceptor() {
        this(new Slf4jEventSender());
    }
    
    public LoggingOutInterceptor(LogEventSender sender) {
        super(Phase.PRE_STREAM, sender);
        addBefore(StaxOutInterceptor.class.getName());
    }
    
    public void setSensitiveProtocolHeaders(Set<String> sensitiveProtocolHeaders) {
		this.sensitiveProtocolHeaders = sensitiveProtocolHeaders;
	}

    public void handleMessage(Message message) throws Fault {
        createExchangeId(message);
        
        if (shouldLogContent(message)) {
            final OutputStream os = message.getContent(OutputStream.class);
            if (os != null) {
                LoggingCallback callback = new LoggingCallback(logEventSender, message, os, logThreshold, discThreshold, memThreshold);
                message.setContent(OutputStream.class, createCachingOut(message, os, callback));
            } else {
                final Writer iowriter = message.getContent(Writer.class);
                if (iowriter != null) { 
                    message.setContent(Writer.class, new LogEventSendingWriter(logEventSender, message, iowriter, logThreshold, discThreshold, memThreshold));
                }
            }
        } else {
            final LogEvent event = logEventMapper.map(message, sensitiveProtocolHeaders);
            event.setPayload(CONTENT_SUPPRESSED);
            logEventSender.send(event);
        }
    }

    private OutputStream createCachingOut(Message message, final OutputStream os, CachedOutputStreamCallback callback) {
        final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
        if (memThreshold != -1) {
            newOut.setThreshold(memThreshold);
        }
        if (discThreshold != -1) {
            newOut.setCacheLimit(discThreshold);
        }
        newOut.registerCallback(callback);
        return newOut;
    }

    private class LogEventSendingWriter extends FilterWriter {
        StringWriter out2;
        int count;
        Message message;
        private LogEventSender sender;
		private int logThreshold;
		private int discThreshold;
		private int memThreshold;

        LogEventSendingWriter(LogEventSender sender, Message message, Writer writer, int logThreshold, int discThreshold, int memThreshold) {
            super(writer);
            this.sender = sender;
            this.message = message;
            if (!(writer instanceof StringWriter)) {
                out2 = new StringWriter();
            }
            this.logThreshold = logThreshold;
            this.discThreshold = discThreshold;
            this.memThreshold = memThreshold;        }

        public void write(int c) throws IOException {
            super.write(c);
            if (out2 != null && count < discThreshold) {
                out2.write(c);
            }
            count++;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            super.write(cbuf, off, len);
            if (out2 != null && count < discThreshold) {
                out2.write(cbuf, off, len);
            }
            count += len;
        }

        public void write(String str, int off, int len) throws IOException {
            super.write(str, off, len);
            if (out2 != null && count < discThreshold) {
                out2.write(str, off, len);
            }
            count += len;
        }

        public void close() throws IOException {
            final LogEvent event = logEventMapper.map(message, Collections.emptySet());
            StringWriter w2 = out2;
            if (w2 == null) {
                w2 = (StringWriter)out;
            }
            
            String payload = w2.toString();
            if(count > payload.length()) {
            	event.setTruncated(true);
            }
            
            filterPayload(event, payload);
            
            sender.send(event);
            
            message.setContent(Writer.class, out);
            
            super.close();
        }

    }

    public class LoggingCallback implements CachedOutputStreamCallback {

        private final Message message;
        private final OutputStream origStream;
        protected final int logThreshold;
        protected final int discThreshold;
        protected final int memThreshold;
        
        private LogEventSender sender;

        public LoggingCallback(final LogEventSender sender, final Message msg, final OutputStream os, int logThreshold, int discThreshold, int memThreshold) {
            this.sender = sender;
            this.message = msg;
            this.origStream = os;
            this.logThreshold = logThreshold;
            this.discThreshold = discThreshold;
            this.memThreshold = memThreshold;
        }

        public void onFlush(CachedOutputStream cos) {

        }

        public void onClose(CachedOutputStream cos) {
            final LogEvent event = logEventMapper.map(message, sensitiveProtocolHeaders);
            copyPayload(cos, event);

            sender.send(event);
            
            try {
                // empty out the cache
                cos.lockOutputStream();
                cos.resetOut(null, false);
            } catch (Exception ex) {
                // ignore
            }
            message.setContent(OutputStream.class, origStream);
        }

        private void copyPayload(CachedOutputStream cos, final LogEvent event) {
            try {
                StringBuilder payload = new StringBuilder((int)cos.size());
                
                String encoding = (String)message.get(Message.ENCODING);
                if (StringUtils.isEmpty(encoding)) {
                    cos.writeCacheTo(payload, discThreshold);
                } else {
                    cos.writeCacheTo(payload, encoding, discThreshold);
                }
                
                if(discThreshold < cos.size()) {
                	event.setTruncated(true);
                }
                
                filterPayload(event, payload.toString());
            } catch (Exception ex) {
                // ignore
            }
        }
        
    }

}
