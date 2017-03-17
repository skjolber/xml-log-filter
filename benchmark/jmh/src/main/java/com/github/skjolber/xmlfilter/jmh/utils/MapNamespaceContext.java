package com.github.skjolber.xmlfilter.jmh.utils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public  final class MapNamespaceContext implements NamespaceContext {
	
    private Map<String, String> namespaces = new HashMap<String, String>();

    public MapNamespaceContext(final Map<String, String> ns) {
        this.namespaces = ns;
    }

    public void addNamespace(final String prefix, final String namespaceURI) {
        this.namespaces.put(prefix, namespaceURI);
    }

    public void addNamespaces(final Map<String, String> ns) {
        this.namespaces.putAll(ns);
    }

    public String getNamespaceURI(String prefix) {
        if (null == prefix) {
            throw new IllegalArgumentException("Null prefix to getNamespaceURI");
        }
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        return namespaces.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Null namespace to getPrefix");
        }
        if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }

        for (Map.Entry<String, String> e : namespaces.entrySet()) {
            if (e.getValue().equals(namespaceURI)) {
                return e.getKey();
            }
        }
        return null;
    }

    public Iterator<String> getPrefixes(String namespaceURI) {
        return null;
    }

    public Map<String, String> getUsedNamespaces() {
        return namespaces;
    }
}