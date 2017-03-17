package com.github.skjolber.xmlfilter.stax.soap;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.stax.AbstractSingleXPathStAXXmlFilter;

public class AbstractSingleXPathStAXSoapHeaderXmlFilter extends AbstractSingleXPathStAXXmlFilter {

	public static final String BODY = "Body";
	
	protected int filterMatches;

	public AbstractSingleXPathStAXSoapHeaderXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type, int filterMatches, XMLInputFactory2 inputFactory, XMLOutputFactory2 outputFactory) {
		super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, expression, type, inputFactory, outputFactory);
		
		if(filterMatches == -1) {
			this.filterMatches = Integer.MAX_VALUE;
		} else {
			this.filterMatches = filterMatches;
		}
	}
	
	public int getFilterMatches() {
		return filterMatches;
	}
	
}
