package com.github.skjolber.xmlfilter.stax;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.github.skjolber.indent.Indent;
import com.github.skjolber.xmlfilter.filter.SingleXPathFilter;

public class AbstractSingleXPathStAXXmlFilter extends SingleXPathFilter {

	protected final XMLInputFactory2 inputFactory;
	protected final XMLOutputFactory2 outputFactory;
	
	public AbstractSingleXPathStAXXmlFilter(boolean declaration, Indent indentation, int maxTextNodeLength, int maxCDATANodeLength, String expression, FilterType type, XMLInputFactory2 inputFactory, XMLOutputFactory2 outputFactory) {
		super(declaration, indentation, maxTextNodeLength, maxCDATANodeLength, expression, type);
		
		this.inputFactory = inputFactory;
		this.outputFactory = outputFactory;
	}
	
	protected void skip(XMLStreamReader2 reader) throws XMLStreamException {
		int level = 1;
		do {
			int event = reader.next();
			if(event == XMLStreamConstants.START_ELEMENT) {
				level++;
			} else if(event == XMLStreamConstants.END_ELEMENT) {
				level--;
			}
		} while(level > 0);

	}

	protected void move(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		do {
			int event = reader.next();
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				writeStartElement(reader, writer);
				break;
			case XMLStreamConstants.END_ELEMENT:
				writer.writeFullEndElement();
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
	
	/*/Envelope/Header/Security/UsernameToken/Password
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:log="http://xmlns.skjolber.github.com/schema/logger">
	<soapenv:Header>
		<wsse:Security
			xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
			xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
			soapenv:mustUnderstand="1">
			<wsse:UsernameToken wsu:Id="UsernameToken-1">
				<wsse:Username>thomas</wsse:Username>
				<wsse:Password
					Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">password</wsse:Password>
			</wsse:UsernameToken>
		</wsse:Security>
	</soapenv:Header>
	<soapenv:Body>
		<log:performLogMessageRequest>
			<samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol">value</samlp:Response>
		</log:performLogMessageRequest>
	</soapenv:Body>
</soapenv:Envelope>
	*/
	
}
