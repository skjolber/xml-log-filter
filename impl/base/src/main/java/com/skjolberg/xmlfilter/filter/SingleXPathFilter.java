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

import com.github.skjolber.indent.Indent;

public abstract class SingleXPathFilter extends AbstractSingleXPathXmlFilter {

	protected final String[] pathStrings;
	protected final String attributeString;
	
	public SingleXPathFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type) {
		super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, expression, type);
		
		if(expression.startsWith(AbstractXPathFilter.ANY_PREFIX)) {
			throw new IllegalArgumentException("Any element expression not supported");
		}

		String[] paths = parse(expression);
		if(paths[paths.length - 1].charAt(0) == '@') {
			//remove @
			if(type == FilterType.PRUNE) {
				throw new IllegalArgumentException("Attribute match XPath for prune not supported");
			}
			this.attributeString = intern(paths[paths.length - 1].substring(1));
			
			String[] elementPath = new String[paths.length - 1];
			System.arraycopy(paths, 0, elementPath, 0, elementPath.length);
			
			paths = elementPath;
		} else {
			this.attributeString = null;
		}
		for(int i = 0; i < paths.length; i++) {
			paths[i] = intern(paths[i]);
		}
		this.pathStrings = paths;
	}

}
