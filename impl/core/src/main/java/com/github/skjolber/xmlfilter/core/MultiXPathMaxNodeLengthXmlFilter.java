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
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondCDataEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondCommentEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondDTDEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondStartElement;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanProcessingInstructionEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.skipSubtree;

import com.github.skjolber.xmlfilter.filter.CharArrayFilter;
import com.github.skjolber.xmlfilter.filter.MultiCharArrayXPathFilter;

/**
 * 
 * XML filter with support for multiple anonymize and prune paths, max text and CDATA node sizes.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public class MultiXPathMaxNodeLengthXmlFilter extends MultiCharArrayXPathFilter {

	public MultiXPathMaxNodeLengthXmlFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, anonymizes, prunes, null);
	}

	public CharArrayFilter ranges(final char[] chars, int offset, int length) {

		/**
		 *
		 * Implementation note: cdata + comments characters handled locally
		 *  
		 */
		
		final int[] elementMatches = new int[elementFilters.length];
		final int[] attributeMatches = new int[attributeFilters.length];

		boolean anon = false;
		
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
								// characters: node text
								if(sourceStart < offset - 1) {
									if(anon) {
										filter.add(sourceStart, offset - 1, FILTER_ANON);
									} else {
										if(offset - sourceStart > maxTextNodeLength) {
											int part = findMaxTextNodeLength(chars, sourceStart, offset, maxTextNodeLength);
											if(offset - 1 - sourceStart - part > 0) {
												filter.add(sourceStart + part, offset - 1, -(offset - 1 - sourceStart - part));
											}
										}
										
									}
								}

							}

							// constrain matches
							if(level < elementFilterStart.length) {
								constrainMatches(elementMatches, level);
								
								anon = matchAnon(elementMatches, level);
							} else {
								anon = false;
							}
							if(level < attributeFilterStart.length) {
								constrainAttributeMatches(attributeMatches, level);
							}

							offset = scanBeyondStartElement(chars, offset + 2, length); // + 3
							
							// complete end element
							sourceStart = offset;
							
							continue;
						}
						case '!': {
							// complete cdata and comments
							
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

								if(anon) {
									if(offset - sourceStart > 0) {
										filter.add(sourceStart + 3,  offset -3, FILTER_ANON);
									}
								} else if(offset - sourceStart > maxCDATANodeLength) {
									int part = findMaxCDATANodeLength(chars, sourceStart, offset - 3, maxCDATANodeLength);
									if(offset - 6 - sourceStart - part > 0) {
										filter.add(sourceStart + part + 3, offset -3, -(offset - 6 - sourceStart - part));
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
							// flush bytes
							sourceStart = offset;
							
							level++;

							boolean prune = false;
							anon = false;
							
							// scan to end of local name
							offset+=1; // skip < and a character
							while(offset < length) {
								if(chars[offset] == ':') {
									// ignore namespace
									sourceStart = offset + 1;
								} else if(isEndOfLocalName(chars[offset])) {
									break;
								}
								offset++;
							}

							if(level < elementFilterStart.length) {
								// match again any higher filter
								if(matchElements(chars, sourceStart, offset, level, elementMatches)) {
									for(int i = elementFilterStart[level]; i < elementFilterEnd[level]; i++) {
										if(elementMatches[i] == level) {
											// matched
											if(elementFilters[i].filterType == FilterType.ANON) {
												anon = true;
											} else if(elementFilters[i].filterType == FilterType.PRUNE) {
												prune = true;
											}
										}
									}
								}
							}
							
							if(anyElementFilters != null) {
								FilterType filterType = matchAnyElements(chars, offset, sourceStart);
								if(filterType == FilterType.ANON) {
									anon = true;
								} else if(filterType == FilterType.PRUNE) {
									prune = true;
								}
							}

							if(level < attributeFilterStart.length) {
								// match again any higher filter
								offset = matchAttributeFilter(chars, offset, length, attributeMatches, filter, sourceStart, level);								
							} else {
								offset = scanBeyondStartElement(chars, offset, length);
								
								// complete start tag
							}

							if(chars[offset - 2] == '/') {
								// empty element
								type = Type.DECREMENT;

								level--;
								
								// constrain matches
								if(level < elementFilterStart.length) {
									constrainMatches(elementMatches, level);
									
									anon = matchAnon(elementMatches, level);
								} else {
									anon = false;
								}
								if(level < attributeFilterStart.length) {
									constrainAttributeMatches(attributeMatches, level);
								}
							} else if(prune) {
								filter.add(offset, offset = skipSubtree(chars, offset, length), FILTER_PRUNE);
								
								type = Type.DECREMENT;
							} else {
								type = Type.INCREMENT;
							}
	
							sourceStart = offset;
							
							continue;
						}
					}	
				}
				
				offset++;
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
