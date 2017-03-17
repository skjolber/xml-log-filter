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

import com.skjolberg.xmlfilter.filter.CharArrayFilter;
import com.skjolberg.xmlfilter.filter.SingleCharArrayXPathFilter;

/**
 * 
 * XML filter with support for a single anonymize XPath-like expression.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class SingleXPathAnonymizeXmlFilter extends SingleCharArrayXPathFilter {

	public SingleXPathAnonymizeXmlFilter(boolean declaration, String expression) {
		super(declaration, null, -1, -1, expression, FilterType.ANON);
	}

	public CharArrayFilter ranges(final char[] chars, int offset, int length) {		

		int matches = 0;
		
		final char[][] elementPaths = this.paths;
		final char[] attribute = this.attribute;
		
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
							if(type == Type.INCREMENT) {
								type = Type.DECREMENT;
								// characters: text node
								if(matches >= elementPaths.length && level == elementPaths.length) {
									if(sourceStart < offset - 1) {
										filter.add(sourceStart, offset - 1, FILTER_ANON);
									}
								}
							}
							level--;

							if(matches >= level) {
								matches = level;
							}
		
							offset = scanBeyondStartElement(chars, offset + 2, length); // + 3
							
							// complete end element
							sourceStart = offset;
							
							continue;
						}
						case '!': {
							// complete cdata and comments so nodes
							
							if(chars[offset + 1] == '-') {
								// look for -->
								
								offset = scanBeyondCommentEnd(chars, offset + 5, length); // + 6
								
								// complete comment
								sourceStart = offset;
	
								type = Type.DECREMENT;

								continue;
							} else if(chars[offset + 1] == '[') {
								// skip <![CDATA[
								sourceStart = offset + 5; // + 9, but count 3 against for the last ]]>
								
								offset = scanBeyondCDataEnd(chars, offset + 10, length); // + 11
								
								if((matches >= elementPaths.length && level == elementPaths.length)) {
									if(offset - sourceStart > 0) {
										filter.add(sourceStart + 3, offset - 3, FILTER_ANON);
									}
								}

								// complete cdata
								sourceStart = offset;

								continue;
							} else {
								// assume entity declaration
								// look for >
								
								offset = scanBeyondDTDEnd(chars, offset + 1, length); // + 2
								type = Type.DECREMENT;
								
								// complete entity declaration
								sourceStart = offset;

								continue;
							}
						}
						case '?' : {
							// processing instruction
	
							offset = scanProcessingInstructionEnd(chars, offset + 1, length); // + 2
							
							// complete processing instruction
							sourceStart = offset;
								
							type = Type.DECREMENT;
							
							continue;
						} 
						default : {
							// start element
							
							if(matches == level && level < elementPaths.length) {
								// first match element, then attribute
								// match element
								
								sourceStart = offset; // start of element name
								
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
									
									// if element matches, match attribute
									if(attribute != null && matches == elementPaths.length) {
										// find start of first attribute
										offset = matchAttribute(chars, offset, length, attribute, filter);
									}
								}
							}
							
							offset = scanBeyondStartElement(chars, offset, length);
							// complete start tag
							
							if(chars[offset - 2] == '/') {
								// empty element
								if(matches >= level) {
									matches = level;
								}
								
								// do not increment level
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
