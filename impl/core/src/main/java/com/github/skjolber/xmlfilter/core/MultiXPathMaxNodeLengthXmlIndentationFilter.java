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

import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.FILTER_ANONYMIZE_MESSAGE_CHARS;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.FILTER_PRUNE_MESSAGE_CHARS;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.FILTER_TRUNCATE_MESSAGE_CHARS;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondCDataEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondCommentEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondDTDEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanBeyondStartElement;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.scanProcessingInstructionEnd;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.skipSubtree;
import static com.github.skjolber.xmlfilter.filter.CharArrayFilter.startsWithXMLDeclaration;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.filter.MultiCharArrayXPathFilter;

/**
 * 
 * XML indentation filter with support for max text and CDATA nodes and multiple anonymize and prune XPath-like expressions.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */


public class MultiXPathMaxNodeLengthXmlIndentationFilter extends MultiCharArrayXPathFilter {

	public MultiXPathMaxNodeLengthXmlIndentationFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes, Indent indent) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, anonymizes, prunes, indent);
	}

	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {

		final int[] elementMatches = new int[elementFilters.length];
		final int[] attributeMatches = new int[attributeFilters.length];

		boolean anon = false;
		
		final int bufferLength = buffer.length();
		
		// use length as the end index
		length += offset;

		int sourceStart = offset;

		int level = 0;
	
		Type type = Type.NEITHER;
		
		try {
			while(offset < length - 3) {
	
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
								// characters: node text
								if(sourceStart < offset) {
									if(anon) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE_CHARS);
										
										sourceStart = offset;
									} else {
										if(offset - sourceStart > maxTextNodeLength) {
											int part = findMaxTextNodeLength(chars, sourceStart, offset, maxTextNodeLength);

											if(offset - sourceStart - part > 0) {
												buffer.append(chars, sourceStart, part);
												buffer.append(FILTER_TRUNCATE_MESSAGE_CHARS);
												buffer.append(offset - sourceStart - part);
												buffer.append(']');
												
												sourceStart = offset; // skip to <
											}
										}
										
									}
								}

								type = Type.DECREMENT;
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

							offset = scanBeyondStartElement(chars, offset + 3, length);
							
							// complete end element
							buffer.append(chars, sourceStart, offset - sourceStart);
							sourceStart = offset;
							
							continue;
						}
						case '!': {
							// complete cdata and comments
							
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

								if(anon) {
									if(offset - 3 - sourceStart > 0) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE_CHARS);
									
										sourceStart = offset - 3; // keep ]]>
									}
								} else if(offset - 3 - sourceStart > maxCDATANodeLength) {
									int part = findMaxCDATANodeLength(chars, sourceStart, offset - 3, maxCDATANodeLength);

									if(offset - 3 - sourceStart - part > 0) {
										buffer.append(chars, sourceStart, part);
										buffer.append(FILTER_TRUNCATE_MESSAGE_CHARS);
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
								
								offset = scanBeyondDTDEnd(chars, offset + 2, length);
								type = Type.DECREMENT;
								
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
		
							level++;

							boolean prune = false;
							anon = false;
							
							// scan to end of local name
							offset+=2; // skip < and a character
							while(offset < length) {
								if(chars[offset] == ':') {
									// ignore namespace
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								} else if(isEndOfLocalName(chars[offset])) {
									break;
								}
								offset++;
							}

							if(level < elementFilterStart.length) {
								// match again any higher filter
								if(matchElements(chars, sourceStart + 1, offset, level, elementMatches)) {
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
								FilterType filterType = matchAnyElements(chars, sourceStart + 1, offset);
								if(filterType == FilterType.ANON) {
									anon = true;
								} else if(filterType == FilterType.PRUNE) {
									prune = true;
								}
							}

							if(level < attributeFilterStart.length) {
								offset = filterAttributes(chars, offset, length, buffer, sourceStart, level, attributeMatches);
							} else {
								offset = scanBeyondStartElement(chars, offset, length);
								
								// complete start tag
								buffer.append(chars, sourceStart, offset - sourceStart);
							}

							sourceStart = offset;

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
								offset = skipSubtree(chars, offset, length);

								buffer.append(FILTER_PRUNE_MESSAGE_CHARS);
								
								type = Type.INCREMENT;
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
