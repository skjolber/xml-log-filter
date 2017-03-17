package com.github.skjolber.xmlfilter.stax;

import java.util.Iterator;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

public class StaxFilterUtility {

	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}
	
	public void writeStartElement(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		String uri = reader.getNamespaceURI();
		String prefix = reader.getPrefix();
		String local = reader.getLocalName();

		writer.writeStartElement(prefix, local, uri);

		// Write out the namespaces
		for (int i = 0; i < reader.getNamespaceCount(); i++) {
			String nsURI = reader.getNamespaceURI(i);
			String nsPrefix = reader.getNamespacePrefix(i);
			
			writer.writeNamespace(nsPrefix, nsURI);
		}
	}

	public void writeAttributes(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		// Write out attributes
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String value = reader.getAttributeValue(i);

			writeAttribute(reader, writer, i, value);
		}
	}

	public void writeAttribute(XMLStreamReader2 reader, XMLStreamWriter2 writer, int i, String value) throws XMLStreamException {
		String ns = reader.getAttributeNamespace(i);
		String nsPrefix = reader.getAttributePrefix(i);
		if(ns != null && nsPrefix != null) {
			writer.writeAttribute(nsPrefix, ns, reader.getAttributeLocalName(i), value);

			// xmlns
		} else if(nsPrefix != null && !nsPrefix.isEmpty()){
			writer.writeAttribute(nsPrefix + ':' + reader.getAttributeLocalName(i), value);
		} else {
			writer.writeAttribute(reader.getAttributeLocalName(i), value);
		}
	}

	public void writeEndElement(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		writer.writeFullEndElement();
	}	
	
    public static boolean isEmpty(String str) {
        if (str != null) {
            int len = str.length();
            for (int x = 0; x < len; ++x) {
                if (str.charAt(x) > ' ') {
                    return false;
                }
            }
        }
        return true;
    }

}
