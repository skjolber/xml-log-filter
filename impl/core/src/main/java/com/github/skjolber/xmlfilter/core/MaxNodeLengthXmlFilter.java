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

import com.skjolberg.xmlfilter.filter.AbstractXmlFilter;
import com.skjolberg.xmlfilter.filter.CharArrayFilter;

/**
 * 
 * XML filter with support for max text and CDATA node sizes.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class MaxNodeLengthXmlFilter extends AbstractXmlFilter {

	public MaxNodeLengthXmlFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength) {
		super(declaration, null, maxTextNodeLength, maxCDATANodeLength);
	}

	public CharArrayFilter ranges(final char[] chars, int offset, int length) {	
		int maxTextNodeLength = this.maxTextNodeLength;
		int maxCDATANodeLength = this.maxCDATANodeLength;
		
		CharArrayFilter filter = new CharArrayFilter();
		
		// use length as the end index
		length += offset;

		int sourceStart = offset;

		int level = 0;
	
		Type type = Type.NEITHER;
		
		try {
			while(offset < length) {
				do {
					if(chars[offset++] == '<') {
						break;
					}
				} while(offset < length);
				if(offset < length) {
					switch(chars[offset]) {
						case '/' : {  // end tag
							level--;
							
							if(type == Type.INCREMENT) {
								type = Type.DECREMENT;
								
								// characters: always text node
								if(offset - sourceStart > maxTextNodeLength) {
									int part = findMaxTextNodeLength(chars, sourceStart, offset, maxTextNodeLength);
									if(offset - 1 - sourceStart - part > 0) {
										filter.add(sourceStart + part, offset - 1, -(offset - 1 - sourceStart - part));
									}
								}
							}

							// complete end element
							sourceStart = offset = scanBeyondStartElement(chars, offset + 2, length); // + 3
							
							continue;
						}
						case '!': {
							// complete cdata and comments so nodes
							switch(chars[offset + 1]) {
							case '[' : {
								// skip <![CDATA[
								sourceStart = offset + 5; // + 9, but count 3 against for the last ]]>
								
								offset = scanBeyondCDataEnd(chars, offset + 10, length); // + 11

								if(offset - sourceStart > maxCDATANodeLength) {
									int part = findMaxCDATANodeLength(chars, sourceStart, offset - 3, maxCDATANodeLength);
									if(offset - sourceStart - part - 6 > 0) {
										filter.add(sourceStart + part + 3, offset - 3, -(offset - sourceStart - part - 6));
									}
								}
								
								// complete cdata
								sourceStart = offset;
								continue;
							}	
							case '-' : {
								// look for -->
									
								// complete comment
								sourceStart = offset = scanBeyondCommentEnd(chars, offset + 5, length); // + 6

								type = Type.DECREMENT;
								
								continue;
							}

								default : {
									// assume entity declaration
									// look for >
									
									// complete entity declaration
									sourceStart = offset = scanBeyondDTDEnd(chars, offset + 1, length); // + 2
									
									type = Type.DECREMENT;				
									
									continue;
								}
							}
						}
						case '?' : {
							// processing instruction
	
							// complete processing instruction
							sourceStart = offset = scanProcessingInstructionEnd(chars, offset + 1, length); // + 2
								
							type = Type.DECREMENT;
							
							continue;
						} 
						default : {
							// start element

							// scan to end of start element
							sourceStart = offset = scanBeyondStartElement(chars, offset + 1, length); // + 2
							
							// see if empty start element
							if(chars[offset - 2] == '/') {
								// empty element
								type = Type.DECREMENT;
								
								// do not increment level
							} else {
								type = Type.INCREMENT;

								level++;
							}
						}
					}	
				}
			}

			if(level != 0) {
				return null;
			}

			return filter;			

		} catch(Exception e) {
			return null;
		}
	}

	

}
