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

package com.skjolberg.xmlfilter.filter;

import static com.skjolberg.xmlfilter.filter.CharArrayFilter.isIndentationWhitespace;

import com.github.skjolber.indent.Indent;

public abstract class SingleCharArrayXPathFilter extends AbstractSingleXPathXmlFilter {

	protected final char[][] paths;
	protected final char[] attribute;
	
	public SingleCharArrayXPathFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type) {
		super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, expression, type);
		
		if(expression.startsWith(AbstractXPathFilter.ANY_PREFIX)) {
			throw new IllegalArgumentException("Any element expression not supported");
		}

		char[][] paths = toCharArray(parse(expression));
		if(paths[paths.length - 1][0] == '@') {
			//remove @
			if(type == FilterType.PRUNE) {
				throw new IllegalArgumentException("Attribute match XPath for prune not supported");
			}
			char[] attribute = new char[paths[paths.length - 1].length - 1];
			System.arraycopy(paths[paths.length - 1], 1, attribute, 0, attribute.length);
			
			this.attribute = intern(attribute);
			
			char[][] elementPath = new char[paths.length - 1][];
			System.arraycopy(paths, 0, elementPath, 0, elementPath.length);
			
			paths = elementPath;
		} else {
			attribute = null;
		}
		for(int i = 0; i < paths.length; i++) {
			paths[i] = intern(paths[i]);
		}
		this.paths = paths;
	}
	
	protected static int matchAttribute(final char[] chars, int offset, int length, final char[] attribute, CharArrayFilter filter) {
		while(offset < length && chars[offset] != '>') {
			// some attributes use ', others " as delimiters
			
			if(isIndentationWhitespace(chars[offset])) {
				
				// skip across whitespace (accept some whitespace)
				do {
					offset++;
				} while(isIndentationWhitespace(chars[offset]));
				
				if(chars[offset] != '/' && chars[offset] != '>') {
					// start of attribute?
					int sourceStart = offset;
					do {
						offset++;

						if(chars[offset] == ':') {
							// ignore namespaces
							offset++;
							sourceStart = offset;
						}

					} while(chars[offset] != '='); // && !isIndentationWhitespace(chars[offset]));
					
					if(matchXPath(chars, sourceStart, offset, attribute)) {
						filter.add(offset + 2, offset = scanToEndOfAttributeValue(chars, offset, length), FILTER_ANON);
					} else {
						offset = scanToEndOfAttributeValue(chars, offset, length);
					}

				}
			} else {
				offset++;
			}
		}
		return offset;
	}
}
