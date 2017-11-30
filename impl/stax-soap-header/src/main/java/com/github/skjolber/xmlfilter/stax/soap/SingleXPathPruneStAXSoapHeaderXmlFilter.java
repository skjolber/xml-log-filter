package com.github.skjolber.xmlfilter.stax.soap;


import java.io.CharArrayReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.github.skjolber.xmlfilter.stax.StringBuilderWriter;
import com.skjolberg.xmlfilter.filter.CharArrayFilter;

public class SingleXPathPruneStAXSoapHeaderXmlFilter extends AbstractSingleXPathStAXSoapHeaderXmlFilter {

	public SingleXPathPruneStAXSoapHeaderXmlFilter(boolean declaration, String expression, int filterMatches, XMLInputFactory2 inputFactory, XMLOutputFactory2 outputFactory) {
		super(declaration, null, -1, -1, expression, FilterType.PRUNE, filterMatches, inputFactory, outputFactory);
	}
	
	@Override
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {
		if(chars.length < offset + length) {
			return false;
		}
		XMLStreamReader2 reader = null;
		XMLStreamWriter2 writer = null;
		try {
			reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(new CharArrayReader(chars, offset, length));
			
			writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(new StringBuilderWriter(output));
			
			copy(reader, writer);
		} catch(Exception e) {
			return false;
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (XMLStreamException e) {
				}
			}
			if(writer != null) {
				try {
					writer.close();
				} catch (XMLStreamException e) {
				}
			}
			
		}
		return true;
	}
	
	public void copy(XMLStreamReader2 reader, XMLStreamWriter2 writer) throws XMLStreamException {
		// number of elements read in

		int filterMatches = 0;
		int matches = 0;
		
		final String[] elementPaths = this.pathStrings;
		
		int level = 0;

		// get the first event so we can print the declaration
		int event = reader.next();
		if(declaration) {
			// does not support 'standalone', but noone uses it
			if(reader.getCharacterEncodingScheme() != null || reader.getVersion() != null) {
				writer.writeStartDocument(reader.getCharacterEncodingScheme(), reader.getVersion());
			}
		}
		do {
			// handle filtering
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				
				if(level == 1 && reader.getLocalName().equals(BODY)) {
					// all headers processed
					move(reader, writer);
					
					return;
				}

				writeStartElement(reader, writer);

				writeAttributes(reader, writer);

				if(matches == level && level < elementPaths.length) {
					// first match element, then attribute
					// match element
					
					if(matchXPath(reader.getLocalName(), elementPaths[matches])) {
						matches++;
						
						// if element matches, match attribute
						if(matches == elementPaths.length) {
							skip(reader);

							writer.writeComment(CharArrayFilter.FILTER_PRUNE_MESSAGE);

							filterMatches++;
							if(this.filterMatches <= filterMatches) {
								move(reader, writer);
								
								return;
							}
							
							// don't increment level
							matches = level;
							
							writer.writeEndElement();
							
							break;
						}
					}
				}

				level++;

				break;
			case XMLStreamConstants.END_ELEMENT:
				level--;
				if(matches >= level) {
					matches = level;
				}
				writer.writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS: {
					String s = reader.getText();
					if (s != null) {
						writer.writeCharacters(s);
					}
				}
				break;
			case XMLStreamConstants.COMMENT:
				writer.writeComment(reader.getText());
				break;
			case XMLStreamConstants.CDATA: {
					String s = reader.getText();
					if (s != null) {
						writer.writeCData(s);
					}
				}
				
				break;
			case XMLStreamConstants.START_DOCUMENT:
				break;
			case XMLStreamConstants.END_DOCUMENT:
				writer.writeEndDocument();
				break;
			case XMLStreamConstants.ENTITY_REFERENCE:
			case XMLStreamConstants.ENTITY_DECLARATION:
			case XMLStreamConstants.ATTRIBUTE:
			case XMLStreamConstants.NAMESPACE:
				throw new IllegalArgumentException("Unsupported event " + event);
			case XMLStreamConstants.PROCESSING_INSTRUCTION: {
				writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
				break;
			}
			default:
				break;
			}
			
			if(!reader.hasNext()) {
				break;
			}
			event = reader.next();
		} while (true);
	}

	
}
