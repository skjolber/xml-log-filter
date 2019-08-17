package com.github.skjolber.xmlfilter.jmh.filter;

import java.util.Iterator;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.filter.AbstractXmlFilter;

public abstract class AbstractStAXXmlIndentationFilter extends AbstractXmlFilter {

	public AbstractStAXXmlIndentationFilter(boolean declaration, Indent indent) {
		super(declaration, indent);
	}

	public static void copy(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
		// number of elements read in

		do {
			int event = reader.next();
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				writeStartElement(reader, writer);
				break;
			case XMLStreamConstants.END_ELEMENT:
				writer.writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS:
				String s = reader.getText();
				if (s != null) {
					writer.writeCharacters(s);
				}
				break;
			case XMLStreamConstants.COMMENT:
				writer.writeComment(reader.getText());
				break;
			case XMLStreamConstants.CDATA:
				writer.writeCData(reader.getText());
				break;
			case XMLStreamConstants.START_DOCUMENT:
				writer.writeStartDocument();
			case XMLStreamConstants.END_DOCUMENT:
				writer.writeEndDocument();
			case XMLStreamConstants.ATTRIBUTE:
			case XMLStreamConstants.NAMESPACE:
				break;
			default:
				break;
			}
		} while (reader.hasNext());
	}

	private static void writeStartElement(XMLStreamReader reader, XMLStreamWriter writer)
			throws XMLStreamException {
		String uri = reader.getNamespaceURI();
		String prefix = reader.getPrefix();
		String local = reader.getLocalName();

		if (prefix == null) {
			prefix = "";
		}

		boolean writeElementNS = false;

		if (uri != null) {
			writeElementNS = true;
			Iterator<String> it = cast(writer.getNamespaceContext().getPrefixes(uri));
			while (it != null && it.hasNext()) {
				String s = it.next();
				if (s == null) {
					s = "";
				}
				if (s.equals(prefix)) {
					writeElementNS = false;
				}
			}
		}

		// Write out the element name
		if (uri != null) {
			if (prefix.length() == 0 && isEmpty(uri)) {
				writer.writeStartElement(local);
			} else {
				writer.writeStartElement(prefix, local, uri);
			}
		} else {
			writer.writeStartElement(local);
		}

		// Write out the namespaces
		for (int i = 0; i < reader.getNamespaceCount(); i++) {
			String nsURI = reader.getNamespaceURI(i);
			String nsPrefix = reader.getNamespacePrefix(i);
			if (nsPrefix == null) {
				nsPrefix = "";
			}
			if (nsURI == null) {
				nsURI = "";
			}
			if (nsPrefix.length() == 0) {
				writer.writeDefaultNamespace(nsURI);
				writer.setDefaultNamespace(nsURI);
			} else {
				writer.writeNamespace(nsPrefix, nsURI);
				writer.setPrefix(nsPrefix, nsURI);
			}

			if (nsURI.equals(uri) && nsPrefix.equals(prefix)) {
				writeElementNS = false;
			}
		}

		// Check if the namespace still needs to be written.
		// We need this check because namespace writing works
		// different on Woodstox and the RI.
		if (writeElementNS) {
			if (prefix.length() == 0) {
				writer.writeDefaultNamespace(uri);
				writer.setDefaultNamespace(uri);
			} else {
				writer.writeNamespace(prefix, uri);
				writer.setPrefix(prefix, uri);
			}
		}        

		// Write out attributes
		for (int i = 0; i < reader.getAttributeCount(); i++) {
			String ns = reader.getAttributeNamespace(i);
			String nsPrefix = reader.getAttributePrefix(i);
			if (ns == null || ns.length() == 0) {
				writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
			} else if (nsPrefix == null || nsPrefix.length() == 0) {
				writer.writeAttribute(reader.getAttributeNamespace(i), reader.getAttributeLocalName(i),
						reader.getAttributeValue(i));
			} else {
				Iterator<String> it = cast(writer.getNamespaceContext().getPrefixes(ns));
				boolean writeNs = true;
				while (it != null && it.hasNext()) {
					String s = it.next();
					if (s == null) {
						s = "";
					}
					if (s.equals(nsPrefix)) {
						writeNs = false;
					}
				}
				if (writeNs) {
					writer.writeNamespace(nsPrefix, ns);
					writer.setPrefix(nsPrefix, ns);
				}
				writer.writeAttribute(reader.getAttributePrefix(i), reader.getAttributeNamespace(i), reader
						.getAttributeLocalName(i), reader.getAttributeValue(i));
			}

		}
	}

    public static <T> Iterator<T> cast(Iterator<?> p) {
        return (Iterator<T>)p;
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
