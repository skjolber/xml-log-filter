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

package com.skjolberg.xmlfilter.core;

import java.util.Arrays;

import com.skjolberg.indent.Indent;

public abstract class AbstractXPathXmlFilter extends AbstractXmlFilter {

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
	
	/** strictly not needed, but necessary for testing */
	protected final String[] anonymizes;
	protected final String[] prunes;
	
	public AbstractXPathXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String[] anonymizes, String[] prunes) {
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

	public AbstractXPathXmlFilter(boolean declaration, Indent indentation, String[] anonymizes, String[] prunes) {
		this(declaration, indentation, Integer.MAX_VALUE, Integer.MAX_VALUE, anonymizes, prunes);
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
	
	protected static char[][] parse(String expression) {
		String[] split = expression.split("/");
		char[][] elementPath = new char[split.length - 1][];
		for(int k = 0; k < elementPath.length; k++) {
			elementPath[k] = split[k + 1].toCharArray();
		}
		return elementPath;
	}

	public String[] getAnonymizeFilters() {
		return anonymizes;
	}

	public String[] getPruneFilters() {
		return prunes;
	}

	@Override
	public String toString() {
		return getClass().getName() + " [" 
				+ "anonymizes=" + Arrays.toString(anonymizes) + ", prunes=" + Arrays.toString(prunes) + ", declaration="
				+ declaration + ", maxTextNodeLength=" + maxTextNodeLength + ", maxCDATANodeLength=" + maxCDATANodeLength
				+ "]";
	}
	
	public static boolean matchXPath(final char[] chars, int start, int end, final char[] attribute) {
		// check if wildcard
		if(attribute.length == 1 && attribute[0] == '*') {
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
	
}
