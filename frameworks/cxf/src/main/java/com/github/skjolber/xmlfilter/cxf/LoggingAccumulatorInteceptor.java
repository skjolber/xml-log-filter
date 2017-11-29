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
import java.io.InputStream;
import java.io.Reader;
import java.io.SequenceInputStream;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedWriter;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * Caches part of an input. 
 * <br> <br>
 * TODO this class is more or less incompatible with a streaming approach.
 *
 */

public class LoggingAccumulatorInteceptor extends AbstractLoggingInterceptor {
	
    protected boolean logBinary;
    protected boolean logMultipart = true;
    
    protected int discThreshold = -1;
    protected int inMemThreshold = -1;

    /**
     * Instantiates a new WireTapIn
     *
     */
    public LoggingAccumulatorInteceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(final Message message) throws Fault {
    	if (shouldLogContent(message)) {
            try {
                InputStream is = message.getContent(InputStream.class);
                if (is != null) {
                    handleInputStream(message, is);
                } else {
                    Reader reader = message.getContent(Reader.class);
                    if (reader != null) {
                        handleReader(message, reader);
                    }
                }
            } catch (Exception e) {
                throw new Fault(e);
            }
    	} else {
    		// just let everything stay as is
    	}
    }

    private void handleReader(Message message, Reader reader) throws IOException {
        CachedWriter writer = new CachedWriter();
        IOUtils.copyAndCloseInput(reader, writer);
        message.setContent(Reader.class, writer.getReader());
        message.setContent(CachedWriter.class, writer);
    }

    private void handleInputStream(Message message, InputStream is) throws IOException {
        CachedOutputStream bos = new CachedOutputStream();
        if (discThreshold > 0) {
            bos.setMaxSize(discThreshold);
        }
        if(inMemThreshold > 0) {
            bos.setThreshold(inMemThreshold);
        }
        // use the appropriate input stream and restore it later
        InputStream bis = is instanceof DelegatingInputStream
            ? ((DelegatingInputStream)is).getInputStream() : is;

        // we can stream the rest
        int limit = Math.max(inMemThreshold, discThreshold);
        copy(bis, bos, limit == -1 ? Integer.MAX_VALUE : limit);
        bos.flush();
        bis = new SequenceInputStream(bos.getInputStream(), bis);

        // restore the delegating input stream or the input stream
        if (is instanceof DelegatingInputStream) {
            ((DelegatingInputStream)is).setInputStream(bis);
        } else {
            message.setContent(InputStream.class, bis);
        }
        message.setContent(CachedOutputStream.class, bos);
    }
    
    private void copy(InputStream bis, CachedOutputStream bos, int length) throws IOException {
		final byte[] buffer = new byte[4096];
		do {
			int read = bis.read(buffer, 0, Math.min(buffer.length, length));
			if(read == -1) {
				break;
			}
			bos.write(buffer, 0, read);
			
			length -= read;
		} while(length > 0);
	}

	public void setDiscThreshold(int discThreshold) {
		this.discThreshold = discThreshold;
	}
    
    public int getDiscThreshold() {
		return discThreshold;
	}
    
    public void setInMemThreshold(int memThreshold) {
		this.inMemThreshold = memThreshold;
	}
    
    public int getInMemThreshold() {
		return inMemThreshold;
	}
    
}
