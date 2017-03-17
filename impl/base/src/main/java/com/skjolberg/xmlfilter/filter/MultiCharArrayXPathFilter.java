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
import static com.skjolberg.xmlfilter.filter.CharArrayFilter.scanBeyondStartElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.skjolber.indent.Indent;

public abstract class MultiCharArrayXPathFilter extends AbstractMultiXPathFilter {

	/** absolute path expressions */
	protected final AbsolutePathFilter[] elementFilters;
	protected final AbsolutePathFilter[] attributeFilters;

	/** any path expression - //element */
	protected final AnyPathFilter[] anyElementFilters;
	
	protected final int[] elementFilterStart;
	protected final int[] elementFilterEnd;
	
	protected final int[] attributeFilterStart;
	protected final int[] attributeFilterEnd;

	public MultiCharArrayXPathFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes, Indent indent) {
		super(declaration, maxTextNodeLength, maxCDATANodeLength, anonymizes, prunes, indent);
		
		List<AbsolutePathFilter> attributes = new ArrayList<AbsolutePathFilter>();
		List<AbsolutePathFilter> elements = new ArrayList<AbsolutePathFilter>();

		List<AnyPathFilter> any = new ArrayList<AnyPathFilter>(); // prunes take precedence of anonymizes

		if(prunes != null) {
			for(int i = 0; i < prunes.length; i++) {
				String prune = prunes[i];
				if(prune.startsWith(ANY_PREFIX)) {
					any.add(new AnyPathFilter(prune.substring(2), FilterType.PRUNE));
				} else {
					elements.add(new AbsolutePathFilter(parse(prune), FilterType.PRUNE));
				}
			}
		}

		if(anonymizes != null) {
			for(int i = 0; i < anonymizes.length; i++) {
				String anonymize = anonymizes[i];
				if(anonymize.startsWith(ANY_PREFIX)) {
					any.add(new AnyPathFilter(anonymize.substring(2), FilterType.ANON));
				} else {
					String[] elementPath = parse(anonymize);
					if(elementPath[elementPath.length - 1].charAt(0) == '@') {
						// remove at
						elementPath[elementPath.length - 1] = elementPath[elementPath.length - 1].substring(1);
	
						attributes.add(new AbsolutePathFilter(elementPath, FilterType.ANON));
					} else {
						elements.add(new AbsolutePathFilter(elementPath, FilterType.ANON));
					}
				}
			}
		}
		
		if(!any.isEmpty()) {
			anyElementFilters = any.toArray(new AnyPathFilter[any.size()]);
		} else {
			anyElementFilters = null;
		}
		
		if(!elements.isEmpty()) {
			Collections.sort(elements, comparator);
			
			int maxElementPaths = Integer.MIN_VALUE;
			for(AbsolutePathFilter elementPath : elements) {
				if(elementPath.getLength() > maxElementPaths) {
					maxElementPaths = elementPath.getLength();
				}
			}
			
			elementFilterStart = new int[maxElementPaths + 1];
			elementFilterEnd = new int[maxElementPaths + 1];
			
			// count
			for(int i = 0; i < elements.size(); i++) {
				if(elementFilterEnd[elements.get(i).getLength()] == 0) { // first filter for this index
					elementFilterStart[elements.get(i).getLength()] = i;
				}
				elementFilterEnd[elements.get(i).getLength()]++;
			}

			// add start to count for end
			for(int i = 0; i < elementFilterEnd.length; i++) {
				elementFilterEnd[i] += elementFilterStart[i];
			}

			elementFilters = elements.toArray(new AbsolutePathFilter[elements.size()]);
		} else {
			elementFilterStart = new int[]{};
			elementFilterEnd = new int[]{};
			elementFilters = new AbsolutePathFilter[]{};
		}

		if(!attributes.isEmpty()) {
			Collections.sort(attributes, comparator);

			int maxAttributePaths = Integer.MIN_VALUE;
			for(AbsolutePathFilter elementPath : attributes) {
				if(elementPath.getLength() > maxAttributePaths) {
					maxAttributePaths = elementPath.getLength();
				}
			}
	
			attributeFilterStart = new int[maxAttributePaths];
			attributeFilterEnd = new int[maxAttributePaths];
			
			// count
			for(int i = 0; i < attributes.size(); i++) {
				
				int elementPaths = attributes.get(i).getLength() - 1; // last is attribute
				if(attributeFilterEnd[elementPaths] == 0) { // first filter for this index
					attributeFilterStart[elementPaths] = i;
				}
				attributeFilterEnd[elementPaths]++;
			}
			
			// add start to count for end
			for(int i = 0; i < attributeFilterEnd.length; i++) {
				attributeFilterEnd[i] += attributeFilterStart[i];
			}
			
			attributeFilters = attributes.toArray(new AbsolutePathFilter[attributes.size()]);
		} else {
			attributeFilterStart = new int[]{};
			attributeFilterEnd = new int[]{};
			attributeFilters = new AbsolutePathFilter[]{};
		}

	}

	protected void constrainAttributeMatches(int[] matches, int level) {
		constrain(attributeFilterStart, matches, level);
	}
	
	protected boolean matchAnon(int[] matches, int level) {
		for(int i = elementFilterStart[level]; i < matches.length; i++) {
			if(matches[i] == elementFilters[i].getLength()) {
				if(elementFilters[i].getFilterType() == FilterType.ANON) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected static void constrain(int[] filter, int[] matches, int level) {
		for(int i = filter[level]; i < matches.length; i++) {
			if(matches[i] > level) {
				matches[i] = level;
			}
		}
	}


	protected void constrainMatches(int[] matches, int level) {
		constrain(elementFilterStart, matches, level);
	}
	
	protected boolean matchElements(final char[] chars, int start, int end, int level, final int[] elementMatches) {
		boolean match = false;
		
		for(int i = elementFilterStart[level]; i < elementMatches.length; i++) {
			if(elementMatches[i] == level - 1) {
				
				if(elementMatches[i] >= elementFilters[i].paths.length) {
					// this filter is at the maximum
					continue;
				}

				if(matchXPath(chars, start, end, elementFilters[i].paths[elementMatches[i]])) {
					elementMatches[i]++;
					
					if(i < elementFilterEnd[level]) {
						match = true;
					}
				}

			}
		}
		return match;
	}

	protected boolean matchElements(final String chars, int level, final int[] elementMatches) {
		boolean match = false;
		
		for(int i = elementFilterStart[level]; i < elementMatches.length; i++) {
			if(elementMatches[i] == level - 1) {
				
				if(elementMatches[i] >= elementFilters[i].paths.length) {
					// this filter is at the maximum
					continue;
				}

				if(matchXPath(chars, elementFilters[i].pathStrings[elementMatches[i]])) {
					elementMatches[i]++;
					
					if(i < elementFilterEnd[level]) {
						match = true;
					}
				}

			}
		}
		return match;
	}

	/**
	 * Note that the order or the filters establishes precedence (prune over anon).
	 * 
	 * @param chars XML characters
	 * @param end XML characters end position
	 * @param start XML characters start position
	 * @return the matching filter type, or null if none
	 */
	
	protected FilterType matchAnyElements(final char[] chars, int end, int start) {
		anyFilters:
		for(int i = 0; i < anyElementFilters.length; i++) {
			if(anyElementFilters[i].path.length != end - start) {
				continue;
			}
			for(int k = 0; k < anyElementFilters[i].path.length; k++) {
				if(anyElementFilters[i].path[k] != chars[start + k]) {
					continue anyFilters;
				}
			}
			
			return anyElementFilters[i].getFilterType();
		}
		return null;
			
	}
	
	/**
	 * Note that the order or the filters establishes precedence (prune over anon).
	 * 
	 * @param chars XML characters
	 * @return the matching filter type, or null if none
	 */
	
	protected FilterType matchAnyElements(final String chars) {
		for(int i = 0; i < anyElementFilters.length; i++) {
			if(anyElementFilters[i].pathString.equals(chars)) {
				return anyElementFilters[i].getFilterType();
			}
		}
		return null;
			
	}	
	
	protected int filterAttributes(final char[] chars, int offset, int length, final StringBuilder buffer, int sourceStart, int level, final int[] attributeMatches) {
		// match again any higher filter
		boolean attributeElementMatch = matchAttributesElements(chars, sourceStart + 1, offset, level, attributeMatches);
		if(attributeElementMatch) {
			int attributeSourceStart = sourceStart;
			// all elements matches, but attribute must match too
			while(offset < length && chars[offset] != '>') {

				// some attributes use ', others " as delimiters

				if(isIndentationWhitespace(chars[offset])) {

					// skip across whitespace (accept some whitespace)
					do {
						offset++;
					} while(isIndentationWhitespace(chars[offset]));

					if(chars[offset] == '/') {
						offset++;
						
						continue;
					} else if(chars[offset] == '>') {
						break;
					}
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

					buffer.append(chars, attributeSourceStart, offset - attributeSourceStart);
					attributeSourceStart = offset;

					offset = scanToEndOfAttributeValue(chars, offset, length);

					// check attribute name, length
					for(int i = attributeFilterStart[level]; i < attributeFilterEnd[level]; i++) {
						if(attributeMatches[i] == level) {
							if(matchXPath(chars, attributeNameStart, attributeSourceStart, attributeFilters[i].paths[level])) {
								buffer.append(chars, attributeSourceStart, 2); // =" or ='
								buffer.append(CharArrayFilter.FILTER_ANONYMIZE_MESSAGE_CHARS);
								attributeSourceStart = offset;
							}
						}
					}

					// flush remainer
					if(attributeSourceStart < offset) {
						buffer.append(chars, attributeSourceStart, offset - attributeSourceStart);
						attributeSourceStart = offset;
					}
				}	
				offset++;
			}
			offset++;
			
			// complete start tag
			if(attributeSourceStart < offset) {
				buffer.append(chars, attributeSourceStart, offset - attributeSourceStart);
			}

		} else {
			offset = scanBeyondStartElement(chars, offset, length);
			
			// complete start tag
			buffer.append(chars, sourceStart, offset - sourceStart);
		}
		return offset;
	}

	protected boolean matchAttributesElements(final char[] chars, int start, int end, int level, final int[] attributeMatches) {
		boolean attributeElementMatch = false;
		for(int i = attributeFilterStart[level]; i < attributeMatches.length; i++) {
			if(attributeMatches[i] == level - 1) {
				if(matchXPath(chars, start, end, attributeFilters[i].paths[level - 1])) {
					attributeMatches[i]++;
					if(i < attributeFilterEnd[level]) {
						attributeElementMatch = true;
					}
				}
			}
		}
		return attributeElementMatch;
	}

	protected boolean matchAttributeName(final char[] chars, int attributeNameStart, int attributeNameEnd, int level, final int[] attributeMatches) {
		// check attribute name, length
		for(int i = attributeFilterStart[level]; i < attributeFilterEnd[level]; i++) {
			if(attributeMatches[i] == level) {
				if(matchXPath(chars, attributeNameStart, attributeNameEnd, attributeFilters[i].paths[level])) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected int matchAttributeFilter(final char[] chars, int offset, int length, final int[] attributeMatches, CharArrayFilter filter, int sourceStart, int level) {
		if(matchAttributesElements(chars, sourceStart, offset, level, attributeMatches)) {
			// all elements matches, but attribute must match too
			while(offset < length && chars[offset] != '>') {

				// some attributes use ', others " as delimiters

				if(isIndentationWhitespace(chars[offset])) {

					// skip across whitespace (accept some whitespace)
					do {
						offset++;
					} while(isIndentationWhitespace(chars[offset]));

					if(chars[offset] == '/') {
						offset++;
						
						continue;
					} else if(chars[offset] == '>') {
						break;
					}
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

					if(matchAttributeName(chars, attributeNameStart, offset, level, attributeMatches)) {
						filter.add(offset + 2, offset = scanToEndOfAttributeValue(chars, offset, length), FILTER_ANON);
					} else {
						offset = scanToEndOfAttributeValue(chars, offset, length);
					}
				}	
				offset++;
			}
			offset++;
			
			// complete start tag
		} else {
			offset = scanBeyondStartElement(chars, offset, length);
			
			// complete start tag
		}
		return offset;
	}
	
	
}
