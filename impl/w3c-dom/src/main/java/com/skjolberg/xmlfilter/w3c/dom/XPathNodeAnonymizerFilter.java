package com.skjolberg.xmlfilter.w3c.dom;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathNodeAnonymizerFilter implements XPathFilter {

	private final XPathExpression xPathExpression;
	
	public XPathNodeAnonymizerFilter(XPathExpression xPathExpression) {
		this.xPathExpression = xPathExpression;
	}
	
	@Override
	public void filter(Document document) throws XPathException {
		Object result = xPathExpression.evaluate(document, XPathConstants.NODESET);
		
		if(result != null) {
			NodeList nodeList = (NodeList)result;
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node item = nodeList.item(i);
				
				anonymize(item);
			}
		} else {
			// do nothing
		}
		
	}

	private void anonymize(Node item) {

		switch(item.getNodeType()) {
		case Node.ELEMENT_NODE : {
			
			// i.e. all attributes and text content
			/*
			if(item.hasAttributes()) {
				NamedNodeMap attributes = item.getAttributes();
				for(int i = 0; i < attributes.getLength(); i++) {
					anonymize(attributes.item(i));
				}
			}
			*/
			
			Node child = item.getFirstChild();
			
			while(child != null) {
				anonymize(child);
				
				child = child.getNextSibling();
			}
			
			break;
		}
		case Node.ATTRIBUTE_NODE : 
		case Node.TEXT_NODE : 
		case Node.CDATA_SECTION_NODE : {
			item.setNodeValue("[*****]");
			
			break;
		}
		default : {
			// do nothing
		}
		}
	}

}
