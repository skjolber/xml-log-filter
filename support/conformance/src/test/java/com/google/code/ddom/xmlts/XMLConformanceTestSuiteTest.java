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
package com.google.code.ddom.xmlts;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.skjolber.ddom.xmlts.XMLConformanceTest;
import com.github.skjolber.ddom.xmlts.XMLConformanceTestSuite;

public class XMLConformanceTestSuiteTest {
	
    private static XMLConformanceTestSuite suite;
    
    @BeforeAll
    public static void load() {
        suite = XMLConformanceTestSuite.newInstance(XMLConformanceTestSuite.class.getResource("/xmlts20130923/xmlconf.xml"));
    }
    
    @Test
    public void testNumberOfTests() {
        Assertions.assertTrue(suite.getAllTests().size() > 2000);
    }
    
    @Test
    public void testIDsAreUnique() {
        Set<String> ids = new HashSet<String>();
        for (XMLConformanceTest test : suite.getAllTests()) {
            String id = test.getId();
            Assertions.assertTrue(ids.add(test.getId()), "Duplicate ID: " + id);
        }
    }
    
    @Test
    public void testLoadTestFiles() throws Exception {
        byte[] buffer = new byte[4096];
        for (XMLConformanceTest test : suite.getAllTests()) {
            InputStream in = test.getInputStream();
            try {
                while (in.read(buffer) != -1) {
                    // Just loop
                }
            } finally {
                in.close();
            }
        }
    }
    

}
