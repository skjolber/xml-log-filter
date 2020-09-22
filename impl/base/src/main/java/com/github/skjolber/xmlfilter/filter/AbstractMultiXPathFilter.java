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

package com.github.skjolber.xmlfilter.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.skjolber.indent.Indent;

public abstract class AbstractMultiXPathFilter extends AbstractXPathFilter {

	public static class AbsolutePathFilter {
		
		public final char[][] paths;
		public final String[] pathStrings;
		public final FilterType filterType;
		
		public AbsolutePathFilter(String[] pathStrings, FilterType filterType) {
			this.paths = toCharArray(pathStrings);
			this.pathStrings = pathStrings;
			this.filterType = filterType;
		}
		
		protected int getLength() {
			return paths.length;
		}
		
		protected FilterType getFilterType() {
			return filterType;
		}
		
	}
	
	public static class AnyPathFilter {
		
		public final String pathString;
		public final char[] path;
		public final FilterType filterType;
		
		public AnyPathFilter(String pathString, FilterType filterType) {
			this.pathString = pathString;
			this.path = intern(pathString.toCharArray());
			this.filterType = filterType;
		}

		protected FilterType getFilterType() {
			return filterType;
		}
	}

	protected static final Comparator<AbsolutePathFilter> comparator = new Comparator<AbsolutePathFilter>() {

		@Override
		public int compare(AbsolutePathFilter o1, AbsolutePathFilter o2) {
			return Integer.compare(o1.getLength(), o2.getLength());
		}
	};
	
	/** absolute path expressions */
	protected final AbsolutePathFilter[] elementFilters;
	protected final AbsolutePathFilter[] attributeFilters;

	/** any path expression - //element */
	protected final AnyPathFilter[] anyElementFilters;
	
	protected final int[] elementFilterStart;
	protected final int[] elementFilterEnd;
	
	protected final int[] attributeFilterStart;
	protected final int[] attributeFilterEnd;

	public AbstractMultiXPathFilter(boolean declaration, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes, Indent indent) {
		super(declaration, indent, maxTextNodeLength, maxCDATANodeLength, anonymizes, prunes);
		
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
			
			for (AbsolutePathFilter absolutePathFilter : elements) {
				elementFilterEnd[absolutePathFilter.getLength()]++;
			}
			
			for(int i = 1; i < elementFilterEnd.length; i++) {
				int sum = 0;
				for(int k = 0; k < i; k++) {
					sum += elementFilterEnd[k];
				}
				
				elementFilterStart[i] = sum;
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
			
			for (AbsolutePathFilter absolutePathFilter : elements) {
				attributeFilterEnd[absolutePathFilter.getLength()]++;
			}
			
			for(int i = 1; i < attributeFilterEnd.length; i++) {
				int sum = 0;
				for(int k = 0; k < i; k++) {
					sum += attributeFilterEnd[k];
				}
				
				attributeFilterStart[i] = sum;
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
	
}
