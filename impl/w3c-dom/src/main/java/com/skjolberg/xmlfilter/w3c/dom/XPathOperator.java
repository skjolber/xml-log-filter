package com.skjolberg.xmlfilter.w3c.dom;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.xml.sax.InputSource;

public class XPathOperator {

    protected XPath xPath;
    protected XPathFactory xPathFactory;

     protected void initFactory() throws XPathFactoryConfigurationException {
        xPathFactory = XPathFactory.newInstance(XPathConstants.DOM_OBJECT_MODEL);
    }

    protected void initXPath(NamespaceContext context) {
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(context);
    }
    public XPathOperator(NamespaceContext context) throws XPathFactoryConfigurationException, ParserConfigurationException {
        initFactory();
        initXPath(context);
    }
    
    public Object evaluate(Object sourceDoc, String expression, QName value) throws XPathExpressionException {

        // create an XPath expression - http://www.zvon.org/xxl/XPathTutorial/General/examples.html
        XPathExpression findStatements = compile(expression);

        // execute the XPath expression against the document
        return findStatements.evaluate(sourceDoc, value);
    }

    public Object evaluate(InputSource sourceDoc, String expression, QName value) throws XPathExpressionException {

        // create an XPath expression - http://www.zvon.org/xxl/XPathTutorial/General/examples.html
        XPathExpression findStatements = compile(expression);

        // execute the XPath expression against the document
        return findStatements.evaluate(sourceDoc, value);
    }

    public XPathExpression compile(String expression) throws XPathExpressionException {
        return xPath.compile(expression);
    }
}