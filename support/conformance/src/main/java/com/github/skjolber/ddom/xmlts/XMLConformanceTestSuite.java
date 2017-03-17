/*
 * Copyright 2009-2011 Andreas Veithen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.skjolber.ddom.xmlts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.skjolber.ddom.collections.Filter;
import com.github.skjolber.ddom.xmlts.XMLConformanceTest.Type;

public class XMLConformanceTestSuite {
	
    private static final Map<String,Type> typeMap = new HashMap<String,Type>();
    
    static {
        typeMap.put("valid", Type.VALID);
        typeMap.put("invalid", Type.INVALID);
        typeMap.put("not-wf", Type.NOT_WELL_FORMED);
        typeMap.put("error", Type.ERROR);
    }
    
    private final Map<String,XMLConformanceTest> tests = new LinkedHashMap<String,XMLConformanceTest>();
    
    private XMLConformanceTestSuite() {}

    public static XMLConformanceTestSuite newInstance(URL url) {
    	return newInstance(url, new HashSet<String>());
    }

    public static XMLConformanceTestSuite newInstance(URL url, Set<String> exclusions) {
        XMLConformanceTestSuite suite = new XMLConformanceTestSuite();
        try {
            suite.load(url, exclusions);
        } catch (Exception ex) {
            throw new RuntimeException("Could not load test suite", ex);
        }
        return suite;
    }
    
    public static Set<String> loadExclusions(URL url) throws IOException {
        Set<String> result = new HashSet<String>();
    	if(url != null) {
	        InputStream openStream = url.openStream();
	        if(openStream != null) {
		        BufferedReader in = new BufferedReader(new InputStreamReader(openStream, "UTF-8"));
		        try {
		            String line;
		            while ((line = in.readLine()) != null) {
		                if (line.length() != 0 && line.charAt(0) != '#') {
		                    result.add(line);
		                }
		            }
		        } finally {
		            in.close();
		        }
	    	}
    	}
        return result;
    }
    
    private void require(Element element, String expected) {
        String actual = element.getTagName();
        if (!actual.equals(expected)) {
            throw new Error("Unexpected element " + actual + "; expected " + expected);
        }
    }
    
    private void load(URL url, Set<String> exclusions) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Element root = documentBuilder.parse(url.toExternalForm()).getDocumentElement();
        require(root, "TESTSUITE");
        Node child = root.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                parseTestCases((Element)child, url, exclusions);
            }
            child = child.getNextSibling();
        }
    }
    
    private void parseTestCases(Element element, URL base, Set<String> exclusions) throws IOException {
        require(element, "TESTCASES");
        String relativeBase = element.getAttribute("xml:base");
        if (relativeBase != null) {
            base = new URL(base, relativeBase);
        }
        Node child = element.getFirstChild();
        while (child != null) {
            if (child instanceof Element) {
                Element childElement = (Element)child;
                if (childElement.getTagName().equals("TESTCASES")) {
                    parseTestCases(childElement, base, exclusions);
                } else {
                    parseTest(childElement, base, exclusions);
                }
            }
            child = child.getNextSibling();
        }
    }
    
    private void parseTest(Element element, URL base, Set<String> exclusions) throws IOException {
        require(element, "TEST");
        String id = element.getAttribute("ID");
        String uri = element.getAttribute("URI");
        String output = element.getAttribute("OUTPUT");
        String version = element.getAttribute("VERSION");
        String edition = element.getAttribute("EDITION");
        Set<XMLVersion> versions;
        if (version.length() == 0 || version.equals("1.0")) {
            if (edition.length() == 0) {
                versions = EnumSet.complementOf(EnumSet.of(XMLVersion.XML_1_1));
            } else {
                String[] editionArray = edition.split(" ");
                XMLVersion[] versionArray = new XMLVersion[editionArray.length];
                for (int i=0; i<editionArray.length; i++) {
                    versionArray[i] = XMLVersion.valueOf("XML_1_0_EDITION_" + editionArray[i]);
                }
                versions = EnumSet.copyOf(Arrays.asList(versionArray));
            }
        } else if (version.equals("1.1")) {
            versions = Collections.singleton(XMLVersion.XML_1_1);
        } else {
            throw new RuntimeException("Unrecognized version " + version);
        }
        tests.put(id, new XMLConformanceTest(
                id,
                exclusions.contains(id) ? Type.EXCLUDED : typeMap.get(element.getAttribute("TYPE")),
                versions,
                !"no".equals(element.getAttribute("NAMESPACE")),
                new URL(base, uri),
                output.length() == 0 ? null : new URL(base, output),
                element.getTextContent()));
    }
    
    public XMLConformanceTest getTest(String id) {
        return tests.get(id);
    }
    
    public Collection<XMLConformanceTest> getAllTests() {
        return Collections.unmodifiableCollection(tests.values());
    }
    
    public Collection<XMLConformanceTest> getTests(Filter<? super XMLConformanceTest> filter) {
    	List<XMLConformanceTest> result = new ArrayList<>(tests.size());
    	for (XMLConformanceTest xmlConformanceTest : tests.values()) {
			if(filter.accept(xmlConformanceTest)) {
				result.add(xmlConformanceTest);
			}
		}
        return result;
    }
    
   
}
