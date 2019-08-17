package com.github.skjolber.xmlfilter.stax.soap;


import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.github.skjolber.xmlfilter.filter.CharArrayFilter;
import com.github.skjolber.xmlfilter.stax.StringBuilderWriter;

public class SingleXPathAnonymizeStAXSoapHeaderXmlFilter extends AbstractSingleXPathStAXSoapHeaderXmlFilter {

	private static class TextEvent {
		String value;
		int type;
		
		public TextEvent(String value, int type) {
			super();
			this.value = value;
			this.type = type;
		}
	}

	public SingleXPathAnonymizeStAXSoapHeaderXmlFilter(boolean declaration, String expression, int filterMatches, XMLInputFactory2 inputFactory, XMLOutputFactory2 outputFactory) {
		super(declaration, null, -1, -1, expression, FilterType.ANON, filterMatches, inputFactory, outputFactory);
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
		boolean anonymize = false;
		int matches = 0;
		
		List<TextEvent> anonymizeContent = new ArrayList<>();
		
		final String[] elementPaths = this.pathStrings;
		final String attribute = this.attributeString;
		
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
			if(anonymize) {
				
				if(event == XMLStreamConstants.END_ELEMENT) {
					for(TextEvent textEvent : anonymizeContent) {
						if(textEvent.type == XMLStreamConstants.CHARACTERS) {
							writer.writeCharacters(CharArrayFilter.FILTER_ANONYMIZE_MESSAGE);
						} else {
							writer.writeCData(CharArrayFilter.FILTER_ANONYMIZE_MESSAGE);
						}
					}
					filterMatches++;
					if(this.filterMatches <= filterMatches) {
						move(reader, writer);
						
						return;
					}
				} else {
					for(TextEvent textEvent : anonymizeContent) {
						if(textEvent.type == XMLStreamConstants.CHARACTERS) {
							writer.writeCharacters(textEvent.value);
						} else {
							writer.writeCData(textEvent.value);
						}
					}
				}
				anonymizeContent.clear();
			}
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				
				if(level == 1 && reader.getLocalName().equals(BODY)) {
					// all headers processed
					move(reader, writer);
					
					return;
				}

				writeStartElement(reader, writer);
				
				boolean filterAttribute = false;
				if(matches == level && level < elementPaths.length) {
					// first match element, then attribute
					// match element
					
					if(matchXPath(reader.getLocalName(), elementPaths[matches])) {
						matches++;
						
						// if element matches, match attribute
						if(matches == elementPaths.length) {
							if(attribute != null) {
								filterAttribute = true;
							} else {
								// anonymize character or cdata content
								anonymize = true;
							}
						}
					}
				}

				if(filterAttribute) {
					// find start of first attribute
					for(int i = 0; i < reader.getAttributeCount(); i++) {
						if(matchXPath(reader.getAttributeLocalName(i), attribute)) {
							writeAttribute(reader, writer, i, CharArrayFilter.FILTER_ANONYMIZE_MESSAGE);
							
							filterMatches++;
						} else {
							writeAttribute(reader, writer, i, reader.getAttributeValue(i));
						}
					}
					
					if(this.filterMatches <= filterMatches) {
						move(reader, writer);
						
						return;
					}
				} else {
					writeAttributes(reader, writer);
				}
				
				level++;
				

				break;
			case XMLStreamConstants.END_ELEMENT:
				anonymize = false;
				
				level--;
				if(matches >= level) {
					matches = level;
				}
				writer.writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS: {
					String s = reader.getText();
					if (s != null) {
						if(anonymize && level == matches) {
							anonymizeContent.add(new TextEvent(s, XMLStreamConstants.CHARACTERS));
						} else {
							writer.writeCharacters(s);
						}
					}
				}
				break;
			case XMLStreamConstants.COMMENT:
				writer.writeComment(reader.getText());
				break;
			case XMLStreamConstants.CDATA: {
					String s = reader.getText();
					if (s != null) {
						if(anonymize) {
							anonymizeContent.add(new TextEvent(s, XMLStreamConstants.CDATA));
						} else {
							writer.writeCData(s);
						}
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
