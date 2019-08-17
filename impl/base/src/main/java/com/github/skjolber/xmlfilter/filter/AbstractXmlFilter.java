/***************************************************************************
 * Copyright 2016 Thomas Rorvik Skjolberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.github.skjolber.xmlfilter.filter;

import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanProcessingInstructionEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.startsWithXMLDeclaration;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.XmlFilter;
public abstract class AbstractXmlFilter implements XmlFilter {
	
	public enum Type {
		/** public for testing */
		INCREMENT(), DECREMENT(), NEITHER();
		
		private Type() {
		}
	}
	
	protected final boolean ignoreWhitespace;
	protected final Indent indent;
	protected final boolean declaration;
	protected final int maxTextNodeLength; // not always in use, if so set to max int
	protected final int maxCDATANodeLength;  // not always in use, if so set to max int
	
	public AbstractXmlFilter(boolean declaration, Indent indentation) {
		this(declaration, indentation, Integer.MAX_VALUE, Integer.MAX_VALUE) ;
	}

	public AbstractXmlFilter(boolean declaration, Indent indent, int maxTextNodeLength, int maxCDATANodeLength) {
		this.declaration = declaration;
		this.indent = indent;
		
		if(maxTextNodeLength < -1) {
			throw new IllegalArgumentException();
		}
		if(maxCDATANodeLength < -1) {
			throw new IllegalArgumentException();
		}
		
		if(maxTextNodeLength == -1) {
			this.maxTextNodeLength = Integer.MAX_VALUE;
		} else {
			this.maxTextNodeLength = maxTextNodeLength;
		}
		if(maxCDATANodeLength == -1) {
			this.maxCDATANodeLength = Integer.MAX_VALUE;
		} else {
			this.maxCDATANodeLength = maxCDATANodeLength;
		}
		
		this.ignoreWhitespace = false;

	}
	
	public boolean getXmlDeclaration() {
		return declaration;
	}
	
	public int getMaxCDATANodeLength() {
		if(maxCDATANodeLength == Integer.MAX_VALUE) {
			return -1;
		}
		return maxCDATANodeLength;
	}
	
	public int getMaxTextNodeLength() {
		if(maxTextNodeLength == Integer.MAX_VALUE) {
			return -1;
		}
		return maxTextNodeLength;
	}

	public boolean process(String xmlString, StringBuilder output) {
		
		char[] chars = xmlString.toCharArray();
		
		return process(chars, 0, chars.length, output);
	}
	
	public String process(char[] chars) {
		
		StringBuilder output = new StringBuilder(chars.length);
		
		if(process(chars, 0, chars.length, output)) {
			return output.toString();
		}
		return null;
	}
	
	public String process(String xmlString) {
		return process(xmlString.toCharArray());
	}
	
	
	public boolean process(Reader reader, int length, StringBuilder output) throws IOException {
		char[] chars = new char[length];

		int offset = 0;
		int read;
		do {
			read = reader.read(chars, offset, length - offset);
			if(read == -1) {
				throw new EOFException("Expected reader with " + length + " characters");
			}

			offset += read;
		} while(offset < length);

		return process(chars, 0, chars.length, output);
	}
	
	public boolean process(Reader reader, StringBuilder output) throws IOException {
		char[] chars = new char[4 * 1024];

		StringWriter writer = new StringWriter(chars.length);
		int offset = 0;
		int read;
		do {
			read = reader.read(chars, offset, chars.length);
			if(read == -1) {
				break;
			}
			
			writer.write(chars, 0, read);

			offset += read;
		} while(true);

		return process(writer.toString(), output);
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		
		// <?xml version="1.0"?>
		if(!declaration && startsWithXMLDeclaration(chars, offset, length)) {
			// skip the whole XML declaration
			// a processing must be at least 4 chars. <? >
			int afterDeclaration = scanProcessingInstructionEnd(chars, offset + 4, length);
			
			length -= afterDeclaration - offset;
			offset = afterDeclaration;
		}
		
		CharArrayFilter copy = ranges(chars, offset, length);
		if(copy == null) {
			return false;
		}
		copy.filter(chars, offset, length, buffer);
		
		return true;
	}
	
	public CharArrayFilter ranges(final char[] chars, int offset, int length) {
		throw new RuntimeException("Not implemented for " + getClass().getName());
	}
	
	public Indent getIndent() {
		return indent;
	}
	
	public boolean getIgnoreWhitespace() {
		return ignoreWhitespace;
	}
	
	

	public static boolean isEndOfLocalName(char c) {
		return c == '>' || c == ' ' || c == '/' || c == '\t' || c == '\n' || c == '\r';
	}
	
	public static int findMaxTextNodeLength(final char[] chars, int sourceStart, int limit, int maxTextNodeLength) {
		// 1. read only whole code points
		// 2. if limit is within an entity, also cut the entity

		int startEntity = -1;
		
		for(int i = sourceStart; i < sourceStart + maxTextNodeLength; i++) {
	        if (chars[i] >= 0xD800) {
	        	// skip extra character, but also allow one more character
	        	i++;
	        	
	        	maxTextNodeLength++;
	        } else if(chars[i] == '&') {
	        	startEntity = i;
	        } else if(chars[i] == ';') {
	        	startEntity = -1;
	        }
		}
		
		if(startEntity != -1) {
			return startEntity - sourceStart;
		}
		return maxTextNodeLength;
	}
	
	public static int findMaxTextNodeLength(final String chars, int maxTextNodeLength) {
		char[] c = new char[maxTextNodeLength];
		chars.getChars(0, maxTextNodeLength, c, 0);
		
		return findMaxTextNodeLength(c, 0, c.length, maxTextNodeLength);
	}
	

	public static int findMaxCDATANodeLength(String chars, int maxCDATANodeLength) {
		char[] c = new char[maxCDATANodeLength];
		chars.getChars(0, maxCDATANodeLength, c, 0);
		
		return findMaxCDATANodeLength(c, 0, c.length, maxCDATANodeLength);
	}

	public static int findMaxCDATANodeLength(char[] chars, int sourceStart, int limit, int maxCDATANodeLength) {
		for(int i = sourceStart; i < sourceStart + maxCDATANodeLength && i < limit; i++) {
	        if (chars[i] >= 0xD800) {
	        	// skip extra character, but also allow one more character
	        	i++;
	        	
	        	maxCDATANodeLength++;
	        }
		}
		return maxCDATANodeLength;
	}	
	
}
