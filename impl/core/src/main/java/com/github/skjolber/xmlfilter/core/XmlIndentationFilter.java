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

package com.github.skjolber.xmlfilter.core;

import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondCDataEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondCommentEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondDTDEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondStartElement;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanProcessingInstructionEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.startsWithXMLDeclaration;

import com.github.skjolber.indent.Indent;
import com.skjolberg.xmlfilter.filter.AbstractXmlFilter;

/**
 * 
 * XML indentation filter.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */


public class XmlIndentationFilter extends AbstractXmlFilter {

	public XmlIndentationFilter(boolean declaration, Indent indent) {
		super(declaration, indent, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public XmlIndentationFilter(boolean declaration) {
		this(declaration, new Indent());
	}
	
	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {

		int bufferLength = buffer.length();
		
		// use length as the end index
		length += offset;

		int sourceStart = offset;

		int level = 0;
	
		Type type = Type.NEITHER;
		
		try {
			while(offset < length) { // ignore potential array index out of bounds
	
				if(chars[offset] == '<') {
					switch(chars[offset + 1]) {
						case '/' : {  // end tag
							level--;
							
							if(type != Type.INCREMENT) {
								// 2 or more endish elements
								// flush bytes
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
		
								indent.append(buffer, level);
							} else {
								type = Type.DECREMENT;
								
								// characters: always text node
							}
		
							offset = scanBeyondStartElement(chars, offset + 3, length);
							
							// complete end element
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;
							
							continue;
						}
						case '!': {
							// complete cdata and comments so nodes
							
							if(chars[offset + 2] == '-') {
								// look for -->
								
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
	
								indent.append(buffer, level);
								
								offset = scanBeyondCommentEnd(chars, offset + 6, length);
								
								// complete comment
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								type = Type.DECREMENT;
	
								continue;
							} else if(chars[offset + 2] == '[') {
								// look for ]]>
								offset = scanBeyondCDataEnd(chars, offset + 11, length); // + 11

								// complete cdata
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								continue;
							} else {
								// assume entity declaration
								// look for >
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
								
								offset = scanBeyondDTDEnd(chars, offset + 2, length);
								type = Type.DECREMENT;
								
								indent.append(buffer, level);
								
								// complete entity declaration
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								continue;
							}
						}
						case '?' : {
							// processing instruction
							// append as start elements
	
							if(sourceStart < offset) {
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							}
	
							offset = scanProcessingInstructionEnd(chars, offset + 2, length);
							
							// <?xml version="1.0"?>
							if(level == 0 && !declaration && startsWithXMLDeclaration(chars, sourceStart, length)) {
								// skip the whole XML declaration
								sourceStart = offset;
							} else {
								indent.append(buffer, level);
	
								// complete processing instruction
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								type = Type.DECREMENT;
							}	
							
							continue;
						} 
						default : {
							// start element
							// flush bytes
							if(sourceStart < offset) {
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
							}
		
							indent.append(buffer, level);
		
							// scan to end of start element
							offset = scanBeyondStartElement(chars, offset + 2, length); 
							
							// see if empty start element
							if(chars[offset - 2] == '/') {
								// empty element
								type = Type.DECREMENT;
								
								// do not increment level
							} else {
								type = Type.INCREMENT;

								level++;
							}
							
							// complete start tag
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;
	
							continue;
						}
					}	
				}
				
				offset++;
			}

			if(level != 0) {
				buffer.setLength(bufferLength);
				
				return false;
			}

			if(sourceStart < length) {
				buffer.append(chars, sourceStart, length - sourceStart);
			}
		} catch(Exception e) {
			buffer.setLength(bufferLength);
			
			return false;
		}
		return true;
	}
	
}
