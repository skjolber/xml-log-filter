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
 * XML filter with support for a single prune XPath-like expression.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */
public class SingleXPathPruneXmlFilter extends SingleCharArrayXPathFilter {

	public SingleXPathPruneXmlFilter(boolean declaration, String expression) {
		super(declaration, null, -1, -1, expression, FilterType.PRUNE);
	}

	public CharArrayFilter ranges(final char[] chars, int offset, int length) {
		/**
		 * 
		 * Note: This filter has the advantage of not keeping track of the current source start
		 * 
		 */
		int matches = 0;
		
		final char[][] elementPaths = this.paths;
		
		length += offset;
		
		CharArrayFilter filter = new CharArrayFilter();
		// use length as the end index

		int level = 0;
	
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
							if(matches >= level) {
								matches = level;
							}
		
							offset = scanBeyondStartElement(chars, offset + 2, length); // + 3
							
							// complete end element
							
							continue;
						}
						case '!': {
							// complete cdata and comments so nodes
							
							if(chars[offset + 1] == '-') {
								// look for -->
								
								// complete comment
								offset = scanBeyondCommentEnd(chars, offset + 5, length); // + 6
	
								continue;
							} else if(chars[offset + 1] == '[') {
								// skip <![CDATA[
								offset = scanBeyondCDataEnd(chars, offset + 10, length); // + 11

								continue;
							} else {
								// assume entity declaration
								// look for >
								
								// complete entity declaration
								offset = scanBeyondDTDEnd(chars, offset + 1, length); // + 2

								continue;
							}
						}
						case '?' : {
							// processing instruction
	
							// complete processing instruction
							offset = scanProcessingInstructionEnd(chars, offset + 1, length); // + 2
							
							continue;
						} 
						default : {
							match:
							if(matches == level && level < elementPaths.length) {
								int sourceStart = offset;

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
							}
							
							offset = scanBeyondStartElement(chars, offset, length);
							
							if(chars[offset - 2] == '/') {
								// empty element
				
								if(matches >= level) {
									matches = level;
								}
								
								// do not increment level
							} else if(matches >= elementPaths.length) {
								// complete start tag
								
								level++;
								
								filter.add(offset, offset = skipSubtree(chars, offset, length), FILTER_PRUNE);
							} else {

								level++;
							}
							
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
