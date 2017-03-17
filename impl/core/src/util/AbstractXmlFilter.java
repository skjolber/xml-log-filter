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

package com.skjolberg.xmlfilter.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

import com.skjolberg.indent.Indent;
import com.skjolberg.xmlfilter.XmlFilter;
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

		boolean success = process(chars, 0, chars.length, output);
		
		return success;
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
		
		Filter copy = ranges(chars, offset, length);
		if(copy == null) {
			return false;
		}
		length += offset;
		for(int i = 0; i < copy.filterIndex; i+=3) {
			buffer.append(chars, offset, copy.filter[i] - offset);
			
			if(copy.filter[i+2] == FILTER_ANON) {
				buffer.append(FILTER_ANONYMIZE_MESSAGE);
			} else if(copy.filter[i+2] == FILTER_PRUNE) {
				buffer.append(FILTER_PRUNE_MESSAGE);
			} else {
				buffer.append(FILTER_TRUNCATE_MESSAGE);
				buffer.append(-copy.filter[i+2]);
				buffer.append(']');
			}
			offset = copy.filter[i + 1];
		}
		
		if(offset < length) {
			buffer.append(chars, offset, length - offset);
		}
		
		return true;
	}
	
	public Filter ranges(final char[] chars, int offset, int length) {
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
	
	/**
	 * 
	 * Scan from start element start to start element end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset  one character past the start tag
	 * @throws ArrayIndexOutOfBoundsException if limit has been reached
	 * 
	 */

	public static final int scanBeyondStartElement(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset++] == '>') {
				return offset;
			}
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end");
	}
	
	public final static int scanBeyondDTDEnd(final char[] chars, int offset, int limit) {
		// assume DTD are nested structures
		// simplified scan loop
		int level = 1;

		while(offset < limit) {
			if(chars[offset] == '<') {
				if(chars[offset + 1] == '!') {
					if(chars[offset + 2] == '-') {
						// comment
						offset = scanBeyondCommentEnd(chars, offset + 2, limit);
					} else {
						level++;
					}
				}
			} else if(chars[offset] == '>') {
				level--;
			} else if(chars[offset] == '"') {
				// scan through next "
				do {
					offset++;
				} while(chars[offset] != '"' && offset < limit);
			} else if(chars[offset] == '\'') {
				// scan through next "
				do {
					offset++;
				} while(chars[offset] != '\'' && offset < limit);
				
			}
			offset++;
			
			if(level == 0) {
				return offset;
			}
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end");
	}

	public final static int scanProcessingInstructionEnd(final char[] chars, int offset, int limit) {
		do {
			offset = scanBeyondStartElement(chars, offset, limit);
			if(chars[offset - 2] == '?') {
				return offset;
			}
		} while(true);
	}
	

	/**
	 * 
	 * Scan one past CDATA end
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the CDATA end
	 * 
	 */
	
	protected static int scanBeyondCDataEnd(final char[] chars, int offset, int limit) {
		return scanBeyondEnd(chars, offset, limit, ']');
	}

	protected static int scanBeyondCommentEnd(final char[] chars, int offset, int limit) {
		return scanBeyondEnd(chars, offset, limit, '-');
	}
	
	private static int scanBeyondEnd(final char[] chars, int offset, int limit, char a) {
		do {
			offset = scanBeyondStartElement(chars, offset, limit);
			if(chars[offset - 2] == a && chars[offset - 3] == a) {
				return offset;
			}
		} while(true);
	}
	
	protected static int skipSubtree(final char[] chars, int offset, int limit) {
		
		int level = 0;
		
		while(offset < limit) {
			
			if(chars[offset] == '<') {
				switch(chars[offset + 1]) {
					case '/' : {  // end tag
						level--;
	
						if(level < 0) {
							return offset;
						}
						
						offset = scanBeyondStartElement(chars, offset + 3, limit);
						
						continue;
					}
					case '!': {
						// complete cdata and comments so nodes
						
						if(chars[offset + 2] == '-') {
							// look for -->
							offset = scanBeyondCommentEnd(chars, offset + 3, limit);
							
							continue;
						} else if(chars[offset + 2] == '[') {
							// look for ]]>
							offset = scanBeyondCDataEnd(chars, offset + 11, limit);
							
							continue;
						} else {
							// do nothing
						}
						break;
					}
					case '?' : {
						// processing instruction
						offset = scanProcessingInstructionEnd(chars, offset + 3, limit);
						
						continue;
					} 
					default : {
						// start element
						// flush bytes
						level++;
	
						// scan to end of start element to see if empty element
						offset += 2; // skip <a in <a>
						while(offset < limit) {
							if(chars[offset] == '>') {
								if(chars[offset - 1] == '/') {
									// empty element
									level--;
								}
								
								offset++;
								
								break;
							}
							offset++;
						}
						
						continue;
					}
				}	
			}
			
			offset++;
		}
		
		return offset;

	}

	public static boolean startsWithXMLDeclaration(final char[] chars, int sourceStart, int sourceEnd) {
		return sourceStart < sourceEnd - 6 && chars[sourceStart + 2] == 'x' && chars[sourceStart + 3] == 'm' && chars[sourceStart + 4] == 'l' && isIndentationWhitespace(chars[sourceStart + 5]);
		// assume method is inlined 
	}

	public static boolean matchRegion(final char[] chars, int start, int end, final char[] attribute) {
		// check if wildcard
		if(attribute.length == end - start) {
			for(int i = 0; i < attribute.length; i++) {
				if(attribute[i] != chars[start + i]) {
					return false;
				}
			}
			return true;
		}
		return false;
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
