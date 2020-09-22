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

import java.util.Arrays;

import com.github.skjolber.indent.Indent;

public abstract class AbstractXPathFilter extends AbstractXmlFilter {

	protected static final int FILTER_PRUNE = 0;
	protected static final int FILTER_ANON = 1;

	public static enum FilterType {
		/** public for testing */
		ANON, PRUNE;
	}
	
	/** 
	 * group 1 starting with slash and containing no special chars except star (*). 
	 * optional group 2 starting with slash and containing no special chars except star (*) and at (@), must be last. 
	 */ 
	protected final static String pruneSyntaxAbsolutePath = "^(\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|]+)+(\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|]+)?$"; // slash + non-special chars @/[]().:|
	protected final static String syntaxAnyPath = "^(\\/\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|\\*]+)$"; // 2x slash + non-special chars @/[]().:|*
	protected final static String anonymizeSyntax = "^(\\/[^@\\/\\[\\]\\(\\)\\.\\:\\|]+)+(\\/[^\\/\\[\\]\\(\\)\\.\\:\\|]+)?$"; // slash + non-special chars @/[]().:|

	protected final static String[] EMPTY = new String[]{};
	public final static String ANY_PREFIX = "//";
	public final static String STAR = "*";
	public final static char[] STAR_CHARS = STAR.toCharArray();
	
	/** strictly not needed, but necessary for testing */
	protected final String[] anonymizes;
	protected final String[] prunes;
	
	public AbstractXPathFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes) {
		super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength);
		
		if(anonymizes == null) {
			anonymizes = EMPTY;
		} else {
			validateAnonymizeExpressions(anonymizes);
		}
		if(prunes == null) {
			prunes = EMPTY;
		} else {
			validatePruneExpressions(prunes);
		}

		this.anonymizes = anonymizes;
		this.prunes = prunes;
	}

	public static void validateAnonymizeExpressions(String[] expressions) {
		for(String expression : expressions) {
			validateAnonymizeExpression(expression);
		}
	}

	public static void validateAnonymizeExpression(String expression) {
		if(!expression.matches(anonymizeSyntax) && !expression.matches(syntaxAnyPath)) {
			throw new IllegalArgumentException("Illegal expression '" + expression + "'. Expected expression on the form /a/b/c or /a/b/@c with wildcards or //a without wildcards");
		}
	}
	
	public static void validatePruneExpressions(String[] expressions) {
		for(String expression : expressions) {
			validatePruneExpression(expression);
		}
	}

	public static void validatePruneExpression(String expression) {
		if(!expression.matches(pruneSyntaxAbsolutePath) && !expression.matches(syntaxAnyPath) ) {
			throw new IllegalArgumentException("Illegal expression '" + expression + "'. Expected expression on the form /a/b/c with wildcards or //a without wildcards");
		}
	}
	
	protected static String[] parse(String expression) {
		String[] split = expression.split("/");
		String[] elementPath = new String[split.length - 1];
		for(int k = 0; k < elementPath.length; k++) {
			elementPath[k] = intern(split[k + 1]);
		}
		return elementPath;
	}

	public String[] getAnonymizeFilters() {
		return anonymizes;
	}

	public String[] getPruneFilters() {
		return prunes;
	}

	public static boolean matchXPath(final char[] chars, int start, int end, final char[] attribute) {
		// check if wildcard, assume interned locally
		if(attribute == STAR_CHARS) {
			return true;
		} else if(attribute.length == end - start) {
			for(int i = 0; i < attribute.length; i++) {
				if(attribute[i] != chars[start + i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static boolean matchXPath(final String chars, final String attribute) {
		// check if wildcard, assume interned locally
		if(attribute == STAR) {
			return true;
		}
		if(!chars.endsWith(attribute)) {
			return false;
		}
		if(chars.length() > attribute.length()) {
			// check local name
			return chars.charAt(chars.length() - attribute.length() - 1) == ':';
		}
		return true;
	}	
	
	public static String intern(String string) {
		if(string.equals(STAR)) {
			return STAR;
		} else {
			return string;
		}
	}
	
	public static char[] intern(char[] chars) {
		if(chars.length == 1 && chars[0] == '*') {
			return STAR_CHARS;
		} else {
			return chars;
		}
	}
	
	public static int scanToEndOfAttributeValue(final char[] chars, int offset, int offsetBounds) {
		// scan to end of attribute value
		if(chars[offset + 1] == '"') {
			offset += 2; // skip =" or ='
			while(chars[offset] != '"') {
				offset++;
			}
		} else {
			offset += 2; // skip =" or ='
			while(chars[offset] != '\'') {
				offset++;
			}
		}
		
		// bounds check is really not necessary as long as all recursive calls rip chars into own array first
		// check bounds for inner-xml cases
		if(offset >= offsetBounds) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		return offset;
	}
	
	protected static char[][] toCharArray(String[] pathStrings) {
		char[][] paths = new char[pathStrings.length][];
		for(int i = 0; i < pathStrings.length; i++) {
			paths[i] = intern(pathStrings[i].toCharArray());
		}
		return paths;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + " [" 
				+ "anonymizes=" + Arrays.toString(anonymizes) + ", prunes=" + Arrays.toString(prunes) + ", declaration="
				+ declaration + ", maxTextNodeLength=" + maxTextNodeLength + ", maxCDATANodeLength=" + maxCDATANodeLength
				+ "]";
	}
	
}
