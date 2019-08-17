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

import com.github.skjolber.indent.Indent;
import com.github.skjolber.indent.IndentFactory;
import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.filter.AbstractXPathFilter;
import com.github.skjolber.xmlfilter.filter.AbstractXmlFilterFactory;
import com.github.skjolber.xmlfilter.filter.AbstractXPathFilter.FilterType;

/**
 * Property maxFilterMatches is ignored.
 * 
 */

public class DefaultXmlFilterFactory extends AbstractXmlFilterFactory {
		
	/**
	 * Spawn a factory instance. Equivalent to using the default constructor.
	 * 
	 * @return newly created {@linkplain DefaultXmlFilterFactory}.
	 */
	
	public static DefaultXmlFilterFactory newInstance() {
		return new DefaultXmlFilterFactory();
	}
	
	/**
	 * Spawn a filter. 
	 * 
	 * @return new, or previously created, thread-safe pretty printer
	 */
	
	public XmlFilter newXmlFilter() {
		// check for any prune/anon filter
		if(ignoreWhitespace) {
			throw new IllegalArgumentException("Ignore whitespace is not supported");
		}
		if(!indent) {
			if(isActiveXPathFilters()) {
				// check for single prune/anon filter
				if(isSinglePruneFilter() && !pruneFilters[0].startsWith(AbstractXPathFilter.ANY_PREFIX)) {
					if(isActiveMaxLength()) {
						return new SingleXPathPruneMaxNodeLengthXmlFilter(xmlDeclaration, pruneFilters[0], maxTextNodeLength, maxCDATANodeLength);
					} else {
						return new SingleXPathPruneXmlFilter(xmlDeclaration, pruneFilters[0]);
					}
				} else if(isSingleAnonymizeFilter() && !anonymizeFilters[0].startsWith(AbstractXPathFilter.ANY_PREFIX)) {
					if(isActiveMaxLength()) {
						return new SingleXPathAnonymizeMaxNodeLengthXmlFilter(xmlDeclaration, anonymizeFilters[0], maxTextNodeLength, maxCDATANodeLength);
					} else {
						return new SingleXPathAnonymizeXmlFilter(xmlDeclaration, anonymizeFilters[0]);
					}
				}
			
				if(isActiveMaxLength()) {
					return new MultiXPathMaxNodeLengthXmlFilter(xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, anonymizeFilters, pruneFilters);
				} else {
					return new MultiXPathXmlFilter(xmlDeclaration, anonymizeFilters, pruneFilters);
				}
			}
			if(isActiveMaxLength() || !xmlDeclaration) {
				return new MaxNodeLengthXmlFilter(xmlDeclaration, maxTextNodeLength, maxCDATANodeLength);
			}

			return new DefaultXmlFilter();
		} else {
			IndentFactory factory = new IndentFactory();
			
			if(indentCharacter != null) {
				factory.setCharacter(indentCharacter);
			}
			if(indentCount != null) {
				factory.setCount(indentCount);
			}
			if(indentPreparedLevels != null) {
				factory.setPreparedLevels(indentPreparedLevels);
			}
			if(indentResetLevel != null) {
				factory.setResetLevel(indentResetLevel);
			}
			if(indentLinebreakType != null) {
				factory.setLinebreakType(indentLinebreakType);
			}
			
			Indent indent = factory.build();
			
			if(isActiveXPathFilters()) {
				// check for single prune/anon filter
				if(isSinglePruneFilter() && !pruneFilters[0].startsWith(AbstractXPathFilter.ANY_PREFIX)) {
					if(isActiveMaxLength()) {
						return new SingleXPathMaxNodeLengthXmlIndentationFilter(xmlDeclaration, pruneFilters[0], FilterType.PRUNE, maxTextNodeLength, maxCDATANodeLength, indent);
					} else {
						return new SingleXPathXmlIndentationFilter(xmlDeclaration, pruneFilters[0], FilterType.PRUNE, indent);
					}
				} else if(isSingleAnonymizeFilter() && !anonymizeFilters[0].startsWith(AbstractXPathFilter.ANY_PREFIX)) {
					if(isActiveMaxLength()) {
						return new SingleXPathMaxNodeLengthXmlIndentationFilter(xmlDeclaration, anonymizeFilters[0], FilterType.ANON, maxTextNodeLength, maxCDATANodeLength, indent);
					} else {
						return new SingleXPathXmlIndentationFilter(xmlDeclaration, anonymizeFilters[0], FilterType.ANON, indent);
					}
				}
			
				if(isActiveMaxLength()) {
					return new MultiXPathMaxNodeLengthXmlIndentationFilter(xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, anonymizeFilters, pruneFilters, indent);
				} else {
					return new MultiXPathXmlIndentationFilter(xmlDeclaration, anonymizeFilters, pruneFilters, indent);
				}
			}
	
			if(isActiveMaxLength()) {
				return new MaxNodeLengthXmlIndentationFilter(xmlDeclaration, maxTextNodeLength, maxCDATANodeLength, indent);
			} else {
				return new XmlIndentationFilter(xmlDeclaration, indent);
			}
		}
	}
	
}
