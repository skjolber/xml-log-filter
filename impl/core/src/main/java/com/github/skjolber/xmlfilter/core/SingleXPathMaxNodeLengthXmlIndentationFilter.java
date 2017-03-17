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

import static com.skjolberg.xmlfilter.filter.CharArrayFilter.FILTER_ANONYMIZE_MESSAGE_CHARS;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.FILTER_PRUNE_MESSAGE_CHARS;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.FILTER_TRUNCATE_MESSAGE_CHARS;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.isIndentationWhitespace;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondCDataEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondCommentEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondDTDEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondStartElement;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanProcessingInstructionEnd;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.skipSubtree;
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.startsWithXMLDeclaration;

import com.github.skjolber.indent.Indent;
import com.skjolberg.xmlfilter.filter.SingleCharArrayXPathFilter;


/**
 * 
 * XML indentation filter with support for max text and CDATA node sizes and a single anonymize or prune XPath-like expression.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */


public class SingleXPathMaxNodeLengthXmlIndentationFilter extends SingleCharArrayXPathFilter {

	public SingleXPathMaxNodeLengthXmlIndentationFilter(boolean declaration, String expression, FilterType type, int maxTextNodeLength, int maxCDATANodeLength, Indent indent) {
		super(declaration, indent, maxTextNodeLength, maxCDATANodeLength, expression, type);
	}
	
	public boolean process(final char[] chars, int offset, int length, final StringBuilder buffer) {
		/**
		 *
		 * Implementation note: cdata + comments characters handled locally
		 *  
		 */
		int matches = 0;
		
		final FilterType filterType = this.filterType;
		final char[][] elementPaths = this.paths;

		final int bufferLength = buffer.length();
		
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
							if(type != Type.INCREMENT) {
								level--;
								// 2 or more endish elements
								// flush bytes
								if(sourceStart < offset) {
									buffer.append(chars, sourceStart, offset - sourceStart);
									sourceStart = offset;
								}
								
								indent.append(buffer, level);
							} else {
								type = Type.DECREMENT;
								// characters: text node
								if(filterType == FilterType.ANON && matches >= elementPaths.length && level == elementPaths.length) {
									if(sourceStart < offset) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE_CHARS);
									
										sourceStart = offset;
									}
								} else {
									
									if(offset - sourceStart > maxTextNodeLength) {
										int part = findMaxTextNodeLength(chars, sourceStart, offset, maxTextNodeLength);

										if(offset - sourceStart - part > 0) {
											buffer.append(chars, sourceStart, part);
											buffer.append(FILTER_TRUNCATE_MESSAGE_CHARS);
											buffer.append(offset - sourceStart - part);
											buffer.append("]");
											
											sourceStart = offset; // skip to <
										}
									}
								}
								level--;
							}
							if(matches >= level) {
								matches = level;
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
								if(offset + 12 >= length) {
									return false;
								}
								
								// skip <![CDATA[ ]]>
								offset += 9;
								
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;
								
								offset = scanBeyondCDataEnd(chars, offset + 2, length);
									
								if((filterType == FilterType.ANON && matches >= elementPaths.length && level == elementPaths.length)) {
									if(offset - sourceStart - 3 > 0) {
										buffer.append(FILTER_ANONYMIZE_MESSAGE_CHARS);
									
										sourceStart = offset - 3; // keep ]]>
									}
								} else if(offset - 3 - sourceStart > maxCDATANodeLength) {
									int part = findMaxCDATANodeLength(chars, sourceStart, offset - 3, maxCDATANodeLength);
									
									if(offset - 3 - sourceStart - part > 0) {
										buffer.append(chars, sourceStart, part);
										buffer.append(FILTER_TRUNCATE_MESSAGE_CHARS);
										buffer.append(offset - 3 - sourceStart - part);
										buffer.append("]");
										
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
		
							match:
							if(matches == level && level < elementPaths.length) {
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
								
								if(matchXPath(chars, sourceStart + 1, offset, elementPaths[matches])) {
									matches++;
								} else {
									break match;
								}
							
								// complete local name
								buffer.append(chars, sourceStart, offset - sourceStart);
								sourceStart = offset;

								if(attribute != null && matches == elementPaths.length) {
									// find start of first attribute
									while(offset < length && chars[offset] != '>') {
										// some attributes use ', others " as delimiters
										
										if(isIndentationWhitespace(chars[offset])) {
											
											// skip across whitespace (accept some whitespace)
											do {
												offset++;
											} while(isIndentationWhitespace(chars[offset]));
											
											if(chars[offset] != '/' && chars[offset] != '>') {
												// start of attribute?
												int attributeNameStart = offset;
												do {
													offset++;
	
													if(chars[offset] == ':') {
														// ignore namespaces
														offset++;
														attributeNameStart = offset;
													}
	
												} while(chars[offset] != '='); // && !isIndentationWhitespace(chars[offset]));
												
												buffer.append(chars, sourceStart, offset - sourceStart);
												sourceStart = offset;
	
												offset = scanToEndOfAttributeValue(chars, offset, length);
												
												if(matchXPath(chars, attributeNameStart, sourceStart, attribute)) {
													buffer.append(chars, sourceStart, 2); // =" or ='
													buffer.append(FILTER_ANONYMIZE_MESSAGE_CHARS);
													sourceStart = offset;
												} else if(sourceStart < offset) {
													// flush remainer
													buffer.append(chars, sourceStart, offset - sourceStart);
													sourceStart = offset;
												}
											}
										} else {
											offset++;
										}
									}

								}
							}
							
							offset = scanBeyondStartElement(chars, offset, length);
							// complete start tag
							buffer.append(chars, sourceStart, offset - sourceStart);
							
							if(chars[offset - 2] == '/') {
								// empty element
								if(matches >= level) {
									matches = level;
								}
								
								type = Type.DECREMENT;
								// do not increment level
							} else if(filterType == FilterType.PRUNE && matches >= elementPaths.length) {
								offset = skipSubtree(chars, offset, length);
								
								level++;

								buffer.append(FILTER_PRUNE_MESSAGE_CHARS);
								
								type = Type.INCREMENT;
							} else {
								type = Type.INCREMENT;

								level++;
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
