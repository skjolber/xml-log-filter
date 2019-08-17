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

import java.util.List;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.indent.LinebreakType;
import com.github.skjolber.indent.PreparedResetIndent;
import com.github.skjolber.indent.ResetIndent;
import com.github.skjolber.xmlfilter.XmlFilter;
import com.github.skjolber.xmlfilter.XmlFilterFactory;

public abstract class AbstractXmlFilterFactory implements XmlFilterFactory {
	
	protected boolean xmlDeclaration = false;

	protected int maxTextNodeLength = -1;
	protected int maxCDATANodeLength = -1;
	
	protected String[] anonymizeFilters;
	protected String[] pruneFilters;

	protected boolean ignoreWhitespace = false;

	// indent 
	protected boolean indent = false;
	protected Character indentCharacter = null;
	protected Integer indentCount = null;
	protected Integer indentPreparedLevels = null;
	protected Integer indentResetLevel = null;
	protected LinebreakType indentLinebreakType = null;
	
	protected int maxFilterMatches = -1;
	
	/**
	 * Spawn a filter. 
	 * 
	 * @return new, or previously created, thread-safe pretty printer
	 */
	
	public abstract XmlFilter newXmlFilter();

	protected boolean isSinglePruneFilter() {
		return (anonymizeFilters == null || anonymizeFilters.length == 0) && pruneFilters.length == 1;
	}

	protected boolean isSingleAnonymizeFilter() {
		return (pruneFilters == null || pruneFilters.length == 0)  && anonymizeFilters.length == 1;
	}

	protected boolean isActiveMaxLength() {
		return maxCDATANodeLength != -1 || maxTextNodeLength != -1;
	}

	protected boolean isActiveXPathFilters() {
		return (anonymizeFilters != null && anonymizeFilters.length > 0) || (pruneFilters != null && pruneFilters.length > 0);
	}

	public void setXmlDeclaration(boolean xmlDeclaration) {
		this.xmlDeclaration = xmlDeclaration;
	}

	/**
	 * 
	 * Set maximum text node length (if not pretty-printed)
	 * 
	 * Note that truncation of nodes below 25 chars will not reduce node size.
	 * 
	 * @param maxTextNodeLength max text node length
	 */

	
	public void setMaxTextNodeLength(int maxTextNodeLength) {
		this.maxTextNodeLength = maxTextNodeLength;
	}
	
	public int getMaxTextNodeLength() {
		return maxTextNodeLength;
	}
	
	/**
	 * Set maximum CDATA node length (if not pretty-printed)
	 * 
	 * Note that truncation of nodes below 25 chars will not reduce node size.
	 * 
	 * @param maxCDATANodeLength max CDATA node length
	 */
	
	public void setMaxCDATANodeLength(int maxCDATANodeLength) {
		this.maxCDATANodeLength = maxCDATANodeLength;
	}
	
	public int getMaxCDATANodeLength() {
		return maxCDATANodeLength;
	}
	
	public boolean isXmlDeclaration() {
		return xmlDeclaration;
	}
	
	/**
	 * Set prune expressions
	 * 
	 * @param filters array of prune expressions
	 */
	
	public void setPruneFilters(String ... filters) {
		if(filters != null) {
			AbstractXPathFilter.validateAnonymizeExpressions(filters);
		}
		
		this.pruneFilters = filters;
	}
	
	/**
	 * Set prune expressions
	 * 
	 * @param filters list of prune expressions
	 */
	
	public void setPruneFilterList(List<String> filters) {
		if(filters != null && !filters.isEmpty()) {
			setPruneFilters(filters.toArray(new String[filters.size()]));
		} else {
			setPruneFilters();
		}
	}
	
	public String[] getPruneFilters() {
		return pruneFilters;
	}
	
	/**
	 * Set anonymize filters
	 * 
	 * @param filters array of anonymize filters
	 */
	
	public void setAnonymizeFilters(String ... filters) {
		if(filters != null) {
			AbstractXPathFilter.validateAnonymizeExpressions(filters);
		}
		
		this.anonymizeFilters = filters;
	}
	
	/**
	 * 
	 * Set anonymize filters
	 * 
	 * @param filters list of anonymize filters
	 */
	
	public void setAnonymizeFilterList(List<String> filters) {
		if(filters != null && !filters.isEmpty()) {
			setAnonymizeFilters(filters.toArray(new String[filters.size()]));
		} else {
			setAnonymizeFilters();
		}
	}
	
	public String[] getAnonymizeFilters() {
		return anonymizeFilters;
	}
	
	public boolean isIgnoreWhitespace() {
		return ignoreWhitespace;
	}

	public void setIndent(boolean indent) {
		this.indent = indent;
	}
	
	public boolean isIndent() {
		return indent;
	}

	public Character getIndentCharacter() {
		return indentCharacter;
	}

	public void setIndentCharacter(Character indentCharacter) {
		this.indentCharacter = indentCharacter;
		this.indent = true;
	}

	public Integer getIndentCount() {
		return indentCount;
	}

	public void setIndentCount(Integer indentCount) {
		this.indentCount = indentCount;
		this.indent = true;
	}

	public Integer getIndentPreparedLevels() {
		return indentPreparedLevels;
	}

	public void setIndentPreparedLevels(Integer indentPreparedLevels) {
		this.indentPreparedLevels = indentPreparedLevels;
		this.indent = true;
	}

	public Integer getIndentResetLevel() {
		return indentResetLevel;
	}

	public void setIndentResetLevel(Integer indentResetLevel) {
		this.indentResetLevel = indentResetLevel;
		this.indent = true;
	}

	public LinebreakType getIndentLinebreakType() {
		return indentLinebreakType;
	}

	public void setIndentLinebreakType(LinebreakType indentLinebreakType) {
		this.indentLinebreakType = indentLinebreakType;
		this.indent = true;
	}

	public void setIgnoreWhitespace(boolean ignoreWhitespace) {
		this.ignoreWhitespace = ignoreWhitespace;
	}

	@Override
	public void setProperty(String name, Object value) throws IllegalArgumentException {
		if(name.equals(IS_INDENT)) {
			if(value instanceof Boolean) {
				setIndent((Boolean) value);
			} else if(value instanceof String) {
				setIndent(Boolean.parseBoolean((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set indent, unexpected value type");
			}
		} else if(name.equals(INDENT_CHARACTER)) {
			if(value instanceof Character) {
				setIndentCharacter((Character) value);
			} else if(value instanceof String) {
				String string = (String)value;
				if(string.length() != 1) {
					throw new IllegalArgumentException("Cannot set indent character to " + value);
				}
				setIndentCharacter(string.charAt(0));
			} else {
				throw new IllegalArgumentException("Cannot set indent character, unexpected value type");
			}
		} else if(name.equals(INDENT_COUNT)) {
			if(value instanceof Integer) {
				setIndentCount((Integer) value);
			} else if(value instanceof String) {
				setIndentCount(Integer.parseInt((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set indent count, unexpected value type");
			}
		} else if(name.equals(INDENT_LINEBREAK)) {
			if(value instanceof LinebreakType) {
				setIndentLinebreakType((LinebreakType) value);
			} else if(value instanceof String) {
				setIndentLinebreakType(LinebreakType.parse((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set indent linebreak, unexpected value type");
			}

		} else if(name.equals(INDENT_PREPARED_LEVELS)) {
			if(value instanceof Integer) {
				setIndentPreparedLevels((Integer) value);
			} else if(value instanceof String) {
				setIndentPreparedLevels(Integer.parseInt((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set indent prepared levels, unexpected value type");
			}
		} else if(name.equals(INDENT_RESET_LEVEL)) {
			if(value instanceof Integer) {
				setIndentResetLevel((Integer) value);
			} else if(value instanceof String) {
				setIndentResetLevel(Integer.parseInt((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set indent reset level, unexpected value type");
			}
		} else if(name.equals(MAX_TEXT_LENGTH)) {
			if(value instanceof Integer) {
				setMaxTextNodeLength((Integer) value);
			} else if(value instanceof String) {
				setMaxTextNodeLength(Integer.parseInt((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set indent reset level, unexpected value type");
			}
		} else if(name.equals(MAX_CDATA_LENGTH)) {
			if(value instanceof Integer) {
				setMaxCDATANodeLength((Integer) value);
			} else if(value instanceof String) {
				setMaxCDATANodeLength(Integer.parseInt((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set max CDATA length, unexpected value type");
			}
		} else if(name.equals(PRUNE)) {
			if(value instanceof String[]) {
				setPruneFilters((String[]) value);
			} else if(value instanceof String) {
				setPruneFilters((String) value);
			} else if(value instanceof List) {
				setPruneFilterList((List<String>) value);
			} else {
				throw new IllegalArgumentException("Cannot set prunes, unexpected value type");
			}
		} else if(name.equals(ANONYMIZE)) {
			if(value instanceof String[]) {
				setAnonymizeFilters((String[]) value);
			} else if(value instanceof String) {
				setAnonymizeFilters((String) value);
			} else if(value instanceof List) {
				setAnonymizeFilterList((List<String>) value);
			} else {
				throw new IllegalArgumentException("Cannot set anonymize, unexpected value type");
			}
		} else if(name.equals(IS_XML_DECLARATION)) {
			if(value instanceof Boolean) {
				setXmlDeclaration((Boolean) value);
			} else if(value instanceof String) {
				setXmlDeclaration(Boolean.parseBoolean((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set XML declaration, unexpected value type");
			}
		} else if(name.equals(MAX_XPATH_MATCHES)) {
			if(value instanceof Integer) {
				setMaxFilterMatches((Integer) value);
			} else if(value instanceof String) {
				setMaxFilterMatches(Integer.parseInt((String) value));
			} else {
				throw new IllegalArgumentException("Cannot set max XPath matches, unexpected value type");
			}
		}
		throw new IllegalArgumentException("Unknown property " + name);
	}

	@Override
	public boolean isPropertySupported(String name) {
		if(name.equals(IS_INDENT)) {
			return true;
		} else if(name.equals(INDENT_CHARACTER)) {
			return true;
		} else if(name.equals(INDENT_COUNT)) {
			return true;
		} else if(name.equals(INDENT_LINEBREAK)) {
			return true;
		} else if(name.equals(INDENT_PREPARED_LEVELS)) {
			return true;
		} else if(name.equals(INDENT_RESET_LEVEL)) {
			return true;
		} else if(name.equals(MAX_TEXT_LENGTH)) {
			return true;
		} else if(name.equals(MAX_CDATA_LENGTH)) {
			return true;
		} else if(name.equals(PRUNE)) {
			return true;
		} else if(name.equals(ANONYMIZE)) {
			return true;
		} else if(name.equals(IS_XML_DECLARATION)) {
			return true;
		} else if(name.equals(MAX_XPATH_MATCHES)) {
			return true;
		}
		return false;
	}
	
	public void setIndent(Indent indent) {
		this.indent = true;
		setIndentCharacter(indent.getCharacter());
		setIndentCount(indent.getCount());
		setIndentLinebreakType(indent.getLinebreakType());
		setIndentPreparedLevels(indent.getPreparedLevels());
		
		if(indent instanceof ResetIndent) {
			ResetIndent resetIndent = (ResetIndent)indent;
			setIndentResetLevel(resetIndent.getResetLevel());
		}

		if(indent instanceof ResetIndent) {
			ResetIndent resetIndent = (ResetIndent)indent;
			setIndentResetLevel(resetIndent.getResetLevel());
		}

		if(indent instanceof PreparedResetIndent) {
			PreparedResetIndent preparedResetIndent = (PreparedResetIndent)indent;
			setIndentResetLevel(preparedResetIndent.getResetLevel());
		}		
	}
	
	public int getMaxFilterMatches() {
		return maxFilterMatches;
	}
	
	public void setMaxFilterMatches(int maxFilterMatches) {
		this.maxFilterMatches = maxFilterMatches;
	}
}
