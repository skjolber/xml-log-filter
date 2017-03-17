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
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.skipSubtree;

import com.skjolberg.xmlfilter.filter.CharArrayFilter;
import com.skjolberg.xmlfilter.filter.SingleCharArrayXPathFilter;

/**
 * 
 * XML filter with support for for max text and CDATA node sizes and a single prune XPath-like expression.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class SingleXPathPruneMaxNodeLengthXmlFilter extends SingleCharArrayXPathFilter {

	public SingleXPathPruneMaxNodeLengthXmlFilter(boolean declaration, String expression, int maxTextNodeLength, int maxCDATANodeLength) {
		super(declaration, null, maxTextNodeLength, maxCDATANodeLength, expression, FilterType.PRUNE);
	}

	public CharArrayFilter ranges(final char[] chars, int offset, int length) {	
		
		int matches = 0;
		
		final char[][] elementPaths = this.paths;

		// use length as the end index
		length += offset;
		
		CharArrayFilter filter = new CharArrayFilter();

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
							if(type == Type.INCREMENT) {
								type = Type.DECREMENT;
								// characters: text node, enforce max length
								if(offset - sourceStart > maxTextNodeLength) {
									int part = findMaxTextNodeLength(chars, sourceStart, offset, maxTextNodeLength);
									if(offset - 1 - sourceStart - part > 0) {
										filter.add(sourceStart + part, offset - 1, -(offset - 1 - sourceStart - part));
									}
								}
							}
							level--;
							if(matches >= level) {
								matches = level;
							}
		
							// complete end element
							sourceStart = offset = scanBeyondStartElement(chars, offset + 2, length); // + 3
							
							continue;
						}
						case '!': {
							// complete cdata and comments so nodes
							
							if(chars[offset + 1] == '-') {
								// look for -->
								
								// complete comment
								sourceStart = offset = scanBeyondCommentEnd(chars, offset + 5, length); // + 6
								
								type = Type.DECREMENT;

								continue;
							} else if(chars[offset + 1] == '[') {
								// skip <![CDATA[
								sourceStart = offset + 5; // + 9, but count 3 against for the last ]]>
								
								offset = scanBeyondCDataEnd(chars, offset + 10, length); // + 11
								
								if(offset - 3 - sourceStart > maxCDATANodeLength) {
									int part = findMaxCDATANodeLength(chars, sourceStart, offset - 3, maxCDATANodeLength);
									if(offset - 6 - sourceStart - part > 0) {
										filter.add(sourceStart + part + 3, offset - 3, -(offset - 6 - sourceStart - part));
									}
								}

								// complete cdata
								sourceStart = offset;

								continue;
							} else {
								// assume entity declaration
								// look for >
								
								// complete entity declaration
								sourceStart = offset = scanBeyondDTDEnd(chars, offset + 1, length); // + 2

								continue;
							}
						}
						case '?' : {
							// processing instruction
	
							// complete processing instruction
							sourceStart = offset = scanProcessingInstructionEnd(chars, offset + 1, length); // + 2
							
							continue;
						} 
						default : {
							// start element

							match:
							if(matches == level && level < elementPaths.length) {
								
								sourceStart = offset;
								
								// scan to end of local name
								offset += 1; // skip < and first character
								while(offset < length) {
									if(chars[offset] == ':') {
										// ignore namespace
										sourceStart = offset + 1;
									} else if(isEndOfLocalName(chars[offset])) {
										break;
									}
									offset++;
								}
								
								if(matchXPath(chars, sourceStart, offset, elementPaths[matches])) {
									matches++;
								} else {
									break match;
								}
							
								// complete local name
								sourceStart = offset;
							}
							
							offset = scanBeyondStartElement(chars, offset, length);
							
							if(chars[offset - 2] == '/') {
								// empty element
					
								if(matches >= level) {
									matches = level;
								}
								
								// do not increment level
								type = Type.DECREMENT;
							} else if(matches >= elementPaths.length) {
								// complete start tag
								
								level++;
								
								filter.add(offset, offset = skipSubtree(chars, offset, length), FILTER_PRUNE);
								
								type = Type.DECREMENT;
							} else {
								type = Type.INCREMENT;

								level++;
							}
							
							sourceStart = offset;
	
							continue;
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
