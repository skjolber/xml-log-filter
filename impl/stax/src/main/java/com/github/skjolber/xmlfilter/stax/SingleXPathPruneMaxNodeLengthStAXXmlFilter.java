package com.github.skjolber.xmlfilter.stax;

import java.io.CharArrayReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.github.skjolber.xmlfilter.filter.CharArrayFilter;

public class SingleXPathPruneMaxNodeLengthStAXXmlFilter extends AbstractSingleXPathStAXXmlFilter {

	public SingleXPathPruneMaxNodeLengthStAXXmlFilter(boolean declaration, String expression, int maxTextNodeLength, int maxCDATANodeLength, XMLInputFactory2 inputFactory, XMLOutputFactory2 outputFactory) {
		super(declaration, null, maxTextNodeLength, maxCDATANodeLength, expression, FilterType.PRUNE, inputFactory, outputFactory);
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
			
			StringBuilderWriter stringBuilderWriter = new StringBuilderWriter(output);
			writer = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(stringBuilderWriter);
			
			copy(reader, writer, stringBuilderWriter);
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
	
	public void copy(XMLStreamReader2 reader, XMLStreamWriter2 writer, StringBuilderWriter stringBuilderWriter) throws XMLStreamException {
		// number of elements read in

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
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:

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

							// don't increment level
							matches = level;
							
							writer.writeFullEndElement();
							
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
				writer.writeFullEndElement();
				break;
			case XMLStreamConstants.CHARACTERS: {
					String s = reader.getText();
					if (s != null) {
						if(s.length() > maxTextNodeLength) {
							int length = findMaxTextNodeLength(s, maxTextNodeLength);
							if(length < s.length()) {
								writer.writeCharacters(s.substring(0, length));
								writer.writeCharacters(CharArrayFilter.FILTER_TRUNCATE_MESSAGE);
								writer.writeCharacters(Integer.toString( s.length() - length));
								writer.writeCharacters("]");
							} else {
								writer.writeCharacters(s);
							}
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
						if(s.length() > maxCDATANodeLength) {
							int findMaxTextNodeLength = findMaxCDATANodeLength(s, maxCDATANodeLength);
							if(s.length() > findMaxTextNodeLength) {
								writer.writeCData(s.substring(0, findMaxTextNodeLength) + CharArrayFilter.FILTER_TRUNCATE_MESSAGE + Integer.toString(s.length() - findMaxTextNodeLength) + "]");
							} else {
								writer.writeCData(s);
							}							
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
