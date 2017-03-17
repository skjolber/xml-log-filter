package com.skjolberg.xmlfilter.w3c.dom;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpression;

public class XPathFilterFactory {

    public XPathFilter getFilter(NamespaceContext context, String[] pruneFilter, String[] anonymizeFilter) throws Exception {

        List<XPathFilter> filters = new ArrayList<XPathFilter>();

        if ((hasNamespaces(pruneFilter) || hasNamespaces(anonymizeFilter)) && context == null) {
        	throw new IllegalArgumentException();
        }

        XPathOperator operator = new XPathOperator(context);

        if (pruneFilter != null) {
            for (String prune : pruneFilter) {
                XPathExpression compile = operator.compile(prune);
                XPathFilter filter = new XPathTreeFilter(compile);
                filters.add(filter);
            }
        }

        if (anonymizeFilter != null) {
            for (String anon : anonymizeFilter) {
                XPathExpression compile = operator.compile(anon);
                XPathFilter filter = new XPathNodeAnonymizerFilter(compile);
                filters.add(filter);
            }
        }

        return new XPathFilterFilterChain(filters);

    }

    /**
     * Simple check for colon in XPath expressions
     *
     * @param filters
     * @return
     */

    private static boolean hasNamespaces(String[] filters) {

        if (filters == null) {
            return false;
        }

        for (String filter : filters) {

            if (filter.contains(":")) {
                return true;
            }
        }

        return false;
    }
}
