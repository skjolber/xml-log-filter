package com.skjolberg.xmlfilter.soap;

import com.github.skjolber.indent.Indent;
import com.skjolberg.xmlfilter.filter.SingleCharArrayXPathFilter;

public class AbstractSingleXPathSoapHeaderXmlFilter extends SingleCharArrayXPathFilter {

	public static final char[] BODY = "Body".toCharArray();
	
	protected int filterMatches;

	public AbstractSingleXPathSoapHeaderXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type, int filterMatches) {
		super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, expression, type);
		
		if(filterMatches == -1) {
			this.filterMatches = Integer.MAX_VALUE;
		} else {
			this.filterMatches = filterMatches;
		}
	}
	

	
}
