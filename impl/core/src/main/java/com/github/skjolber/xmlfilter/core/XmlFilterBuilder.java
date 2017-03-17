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
import com.skjolberg.xmlfilter.XmlFilter;

public class XmlFilterBuilder {

	public static XmlFilterBuilder newXmlFilter() {
		return new XmlFilterBuilder();
	}
	
	private DefaultXmlFilterFactory factory = new DefaultXmlFilterFactory();
	
	public XmlFilter build() {
		return factory.newXmlFilter();
	}

	public XmlFilterBuilder keepXMLDeclaration() {
		factory.setXmlDeclaration(true);
		
		return this;
	}
	
	public XmlFilterBuilder maxCDataNodeLength(int length) {
		factory.setMaxCDATANodeLength(length);
		
		return this;
	}

	public XmlFilterBuilder maxTextNodeLength(int length) {
		factory.setMaxTextNodeLength(length);
		
		return this;
	}

	public XmlFilterBuilder maxNodeLength(int length) {
		factory.setMaxTextNodeLength(length);
		factory.setMaxCDATANodeLength(length);
		
		return this;
	}
	
	public XmlFilterBuilder prune(String ... filter) {
		factory.setPruneFilters(filter);
		
		return this;
	}
	
	public XmlFilterBuilder anonymize(String ... filter) {
		factory.setAnonymizeFilters(filter);
		
		return this;
	}
	
	public XmlFilterBuilder withIdent() {
		factory.setIndent(true);
		
		return this;
	}

	public XmlFilterBuilder withSpaceIndent(int count) {
		factory.setIndentCharacter(' ');
		factory.setIndentCount(count);
		
		return this;
	}

	public XmlFilterBuilder withTabIndent() {
		factory.setIndentCharacter('\t');
		factory.setIndentCount(1);
		
		return this;
	}

	public XmlFilterBuilder with(char character, int count) {
		factory.setIndentCharacter(character);
		factory.setIndentCount(count);
		
		return this;
	}
	
	public XmlFilterBuilder withIdent(Indent indent) {
		factory.setIndent(indent);
		
		return this;
	}
	
}
