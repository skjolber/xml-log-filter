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

import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public abstract class AbstractLoggingInterceptor extends AbstractPhaseInterceptor<Message> {
	
    private static final Set<String> BINARY_CONTENT_MEDIA_TYPES;
    static {
        BINARY_CONTENT_MEDIA_TYPES = new HashSet<String>();
        BINARY_CONTENT_MEDIA_TYPES.add("application/octet-stream");
        BINARY_CONTENT_MEDIA_TYPES.add("image/png");
        BINARY_CONTENT_MEDIA_TYPES.add("image/jpeg");
        BINARY_CONTENT_MEDIA_TYPES.add("image/gif");
    }
    private static final String MULTIPART_CONTENT_MEDIA_TYPE = "multipart";
    protected static final String CONTENT_SUPPRESSED = "--- Content suppressed ---";
    
    public static boolean isBinaryContent(Message message) {
        String contentType = safeGet(message, Message.CONTENT_TYPE);
        return contentType != null && BINARY_CONTENT_MEDIA_TYPES.contains(contentType);
    }
    
    public static boolean isMultipartContent(Message message) {
        String contentType = safeGet(message, Message.CONTENT_TYPE);
        return contentType != null && contentType.startsWith(MULTIPART_CONTENT_MEDIA_TYPE);
    }

    private static String safeGet(Message message, String key) {
        if (!message.containsKey(key)) {
            return null;
        }
        Object value = message.get(key);
        return (value instanceof String) ? value.toString() : null;
    }

    protected int logThreshold = -1; // maximum payload logged (after filtering, in characters)
    protected int discThreshold = -1; // maximum payload held on disc (before filtering, in bytes)
    protected int memThreshold = -1; // maximum payload held in memory (before filtering, in bytes)
    
    protected boolean logBinary;
    protected boolean logMultipart = true;

    public AbstractLoggingInterceptor(String phase) {
        super(phase);
    }

    public void setInMemThreshold(int t) {
        this.memThreshold = t;
    }

    public int getInMemThreshold() {
        return memThreshold;
    }

    protected boolean shouldLogContent(Message message) {
    	return shouldLogContent(isBinaryContent(message), isMultipartContent(message));
    }
    
    protected boolean shouldLogContent(LogEvent event) {
    	return shouldLogContent(event.isBinaryContent(), event.isMultipartContent());
    }

    protected boolean shouldLogContent(boolean binaryContent, boolean multipartContent) {
        return binaryContent && logBinary 
            || multipartContent && logMultipart
            || !binaryContent && !multipartContent;
    }

    public void setLogBinary(boolean logBinary) {
        this.logBinary = logBinary;
    }
    
    public void setLogMultipart(boolean logMultipart) {
        this.logMultipart = logMultipart;
    }

    public void setDiscThreshold(int discThreshold) {
		this.discThreshold = discThreshold;
	}
    
    public void setLogThreshold(int logThreshold) {
		this.logThreshold = logThreshold;
	}
    
    public int getMemThreshold() {
		return memThreshold;
	}
    
    public int getLogThreshold() {
		return logThreshold;
	}

}
