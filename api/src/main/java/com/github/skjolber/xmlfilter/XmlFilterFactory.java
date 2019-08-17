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
package com.github.skjolber.xmlfilter;

public interface XmlFilterFactory {

	public static final String IS_INDENT = "com.skjolberg.xmlfilter.isIndent";
	public static final String INDENT_CHARACTER = "com.skjolberg.xmlfilter.indentCharacter";
	public static final String INDENT_COUNT = "com.skjolberg.xmlfilter.indentCount";
	public static final String INDENT_LINEBREAK = "com.skjolberg.xmlfilter.indentLineBreak";
	public static final String INDENT_PREPARED_LEVELS = "com.skjolberg.xmlfilter.indentPreparedLevels";
	public static final String INDENT_RESET_LEVEL = "com.skjolberg.xmlfilter.indentResetLevel";
	public static final String MAX_TEXT_LENGTH = "com.skjolberg.xmlfilter.maxTextLength";
	public static final String MAX_CDATA_LENGTH = "com.skjolberg.xmlfilter.maxCdataLength";
	public static final String MAX_XPATH_MATCHES = "com.skjolberg.xmlfilter.maxXPathMatches";
	public static final String PRUNE = "com.skjolberg.xmlfilter.prune";
	public static final String ANONYMIZE = "com.skjolberg.xmlfilter.anonymize";
	public static final String IS_XML_DECLARATION = "com.skjolberg.xmlfilter.isXmlDeclaration";
	
	/**
	 * Create new {@linkplain XmlFilter} instance.
	 * 
	 * @return newly created {@linkplain XmlFilter}
	 */

	XmlFilter newXmlFilter();

	/**
	 * Allows the user to set specific feature/property on the underlying
	 * implementation. The underlying implementation is not required to support
	 * every setting of every property in the specification and may use
	 * IllegalArgumentException to signal that an unsupported property may not be
	 * set with the specified value.
	 * 
	 * @param name The name of the property (may not be null)
	 * @param value The value of the property
	 * @throws java.lang.IllegalArgumentException if the property is not supported
	 */

	void setProperty(java.lang.String name, Object value) throws java.lang.IllegalArgumentException;

	/**
	 * Query the set of properties that this factory supports.
	 *
	 * @param name The name of the property (may not be null)
	 * @return true if the property is supported and false otherwise
	 */
	boolean isPropertySupported(String name);

}
