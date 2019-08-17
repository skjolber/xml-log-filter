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

import com.github.skjolber.indent.Indent;

public abstract class AbstractSingleXPathXmlFilter extends AbstractXPathFilter {

	protected final FilterType filterType;

	public AbstractSingleXPathXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type) {
		super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, type == FilterType.ANON ? new String[]{expression} : EMPTY, type == FilterType.PRUNE ? new String[]{expression} : EMPTY);
		
		this.filterType = type;
	}

}
