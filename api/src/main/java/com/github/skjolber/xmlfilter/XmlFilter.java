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

import java.io.IOException;
import java.io.Reader;

/**
 * Interface for filtering XML.
 * 
 * @author Thomas Rorvik Skjolberg
 * 
 */

public interface XmlFilter {

	/**
	 * Filter XML characters to an output StringBuilder.
	 * 
	 * @param chars characters containing XML to be pretty printed
	 * @return a StringBuilder instance filtering was successful, null otherwise.
	 */

	String process(char[] chars);

	/**
	 * Filter XML characters to an output StringBuilder.
	 * 
	 * @param chars characters containing XML to be pretty printed
	 * @return a StringBuilder instance filtering was successful, null otherwise.
	 */

	String process(String chars);
	
	/**
	 * Filter XML characters to an output StringBuilder.
	 * 
	 * @param chars characters containing XML to be pretty printed
	 * @param output the buffer to which indented XML is appended
	 * @return true if filtering was successful. If false, the output buffer is unaffected.
	 */

	boolean process(String chars, StringBuilder output);

	/**
	 * Filter XML characters to an output StringBuilder.
	 * 
	 * @param chars characters containing XML to be pretty printed
	 * @param offset the offset within the chars where the XML starts
	 * @param length the length of the XML within the chars
	 * @param output the buffer to which indented XML is appended
	 * @return true if filtering was successful. If false, the output buffer is unaffected.
	 */

	boolean process(char[] chars, int offset, int length, StringBuilder output);

	/**
	 * Filter XML characters to an output StringBuilder.
	 * 
	 * @param reader reader containing XML characters to be pretty printed
	 * @param length the number of characters within the reader
	 * @param output the buffer to which indented XML is appended
	 * @throws IOException from reader
	 * @return true if filtering was successful. If false, the output buffer is unaffected.
	 */

	boolean process(Reader reader, int length, StringBuilder output) throws IOException;
}
