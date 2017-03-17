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
import com.skjolberg.xmlfilter.filter.CharArrayFilter;

/**
 * 
 * XML indentation filter with support for max text and CDATA node sizes.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */


public class MaxNodeLengthXmlIndentationFilter extends AbstractXmlFilter {

	public MaxNodeLengthXmlIndentationFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, Indent indent) {
		super(declaration, indent, maxTextNodeLength, maxCDATANodeLength);
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		int bufferLength = buffer.length();
		
		// use length as the end index
		length += offset;

		int sourceStart = offset;

		int level = 0;
	
		Type type = Type.NEITHER;
		
		try {
			while(offset < length) {
	
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
								
								if(offset - sourceStart > maxTextNodeLength) {
									int part = findMaxTextNodeLength(chars, sourceStart, offset, maxTextNodeLength);
									
									if(offset - sourceStart - part > 0) {
										buffer.append(chars, sourceStart, part);
										buffer.append(CharArrayFilter.FILTER_TRUNCATE_MESSAGE_CHARS);
										buffer.append(offset - sourceStart - part);
										buffer.append(']');
										
										sourceStart = offset; // skip to <
									}
								}
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
								if(offset + 12 >= length) {
									return false;
								}

								offset += 9; // skip <![CDATA[
								
								// flush <![CDATA[
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								offset = scanBeyondCDataEnd(chars, offset + 2, length); // skip ]]>

								if(offset - 3 - sourceStart > maxCDATANodeLength) {
									int part = findMaxCDATANodeLength(chars, sourceStart, offset - 3, maxCDATANodeLength);

									if(offset - 3 - sourceStart - part > 0) {
										buffer.append(chars, sourceStart, part);
										buffer.append(CharArrayFilter.FILTER_TRUNCATE_MESSAGE_CHARS);
										buffer.append(offset - 3 - sourceStart - part);
										buffer.append(']');
										
										sourceStart = offset - 3; // keep ]]>
									}
								}
								
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
							offset = scanBeyondStartElement(chars, offset, length); 
							
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
