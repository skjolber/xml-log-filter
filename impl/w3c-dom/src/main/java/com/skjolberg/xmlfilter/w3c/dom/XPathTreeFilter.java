package com.skjolberg.xmlfilter.w3c.dom;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathTreeFilter implements XPathFilter {

	private final XPathExpression xPathExpression;
	
	public XPathTreeFilter(XPathExpression xPathExpression) {
		this.xPathExpression = xPathExpression;
	}

	@Override
	public void filter(Document document) throws XPathException {
		
		Object result = xPathExpression.evaluate(document, XPathConstants.NODESET);
		
		if(result != null) {
			NodeList results = (NodeList)result;
			
			for(int i = 0; i < results.getLength(); i++) {
				Node item = results.item(i);

				switch(item.getNodeType()) {
				case Node.ELEMENT_NODE : {
					Element element = (Element)item;

					NodeList childNodes = element.getChildNodes(); // node list is updated
					
					while(childNodes.getLength() > 0) {
						element.removeChild(childNodes.item(0));
					}
					
					Comment comment = document.createComment(" [SUBTREE REMOVED] ");
					
					element.appendChild(comment);
				}
				}
			}
		} else {
			// do nothing
		}
		
	}

}
