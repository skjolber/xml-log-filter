package com.github.skjolber.xmlfilter.jmh.filter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.staxutils.PrettyPrintXMLStreamWriter;
import org.apache.cxf.staxutils.StaxUtils;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.jmh.utils.StringBuilderWriter;
import com.skjolberg.xmlfilter.filter.AbstractXmlFilter;

/**
 * Pretty-printed adopted from PrettyLoggingFilter
 * 
 * @author thomas
 *
 */

public class CxfXmlIndentationFilter extends AbstractXmlFilter {
	
	public CxfXmlIndentationFilter(boolean declaration, Indent indent) {
		super(declaration, indent);
	}

	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder builder) {
		
		try {
	        StringBuilderWriter swriter = new StringBuilderWriter(builder);
	        // Using XMLStreamWriter instead of Transformer as it works with non well formed xml
	        // that can occur when we set a limit and cur the rest off
	        XMLStreamWriter xwriter = StaxUtils.createXMLStreamWriter(swriter);
	        xwriter = new PrettyPrintXMLStreamWriter(xwriter, 2);
	        InputStream in = new ByteArrayInputStream(new String(chars, offset, length).getBytes("UTF-8"));
	        try {
	            StaxUtils.copy(new StreamSource(in), xwriter);
	        } catch (XMLStreamException xse) {
	            //ignore
	        } finally {
	            try {
	                xwriter.flush();
	                xwriter.close();
	            } catch (XMLStreamException xse2) {
	                //ignore
	            }
	            in.close();
	        }
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	



}
