package com.skjolberg.xmlfilter.w3c.dom;

import java.util.LinkedList;

import javax.xml.xpath.XPathException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class XPathMaxLengthNodeFilter implements XPathFilter {
	
	public XPathMaxLengthNodeFilter newCDATAMaxLengthFilter(int limit) {
		return new XPathMaxLengthNodeFilter(limit, Node.CDATA_SECTION_NODE);
	}

	public XPathMaxLengthNodeFilter newTextMaxLengthFilter(int limit) {
		return new XPathMaxLengthNodeFilter(limit, Node.TEXT_NODE);
	}

	protected final int maxNodeLength; // not always in use, if so set to max int
	protected final short nodeType;

	public XPathMaxLengthNodeFilter(int maxNodeLength, short nodeType) {
		this.maxNodeLength = maxNodeLength;
		this.nodeType = nodeType;
	}

	@Override
	public void filter(Document document) throws XPathException {
		
		Element documentElement = document.getDocumentElement();
		
		LinkedList<Node> elements = new LinkedList<>();
		elements.add(documentElement);

		while(!elements.isEmpty()) {
			Node element = elements.removeFirst();
			Node child = element.getFirstChild();
			while(child != null) {
				if(child instanceof Element) {
					elements.add(child);
				} else if(child.getNodeType() == nodeType) {
					limit((Text)child);
				}
				child = child.getNextSibling();
			}
		}
	}

	private void limit(Text child) {
		String nodeValue = child.getNodeValue();
		if(nodeValue.length() > maxNodeLength) {
			String truncated = nodeValue.substring(0, maxNodeLength) + "...[TRUNCATED BY " + (nodeValue.length() - maxNodeLength) + "]";
			child.setTextContent(truncated);
		}
 	}

}
