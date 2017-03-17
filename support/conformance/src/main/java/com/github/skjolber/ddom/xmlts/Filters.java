/*
 * Copyright 2009 Andreas Veithen
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

import com.github.skjolber.ddom.collections.AndFilter;
import com.github.skjolber.ddom.collections.ExclusionFilter;
import com.github.skjolber.ddom.collections.Filter;
import com.github.skjolber.ddom.xmlts.XMLConformanceTest.Type;

public class Filters {
    private Filters() {}
    
    /**
     * Default XML version filter. This selects tests for XML 1.0 4th edition and XML 1.1.
     */
    public static final XMLVersionFilter DEFAULT_VERSION_FILTER = new XMLVersionFilter(XMLVersion.XML_1_0_EDITION_4, XMLVersion.XML_1_1);
    
    public static final Filter<? super XMLConformanceTest> DEFAULT = new AndFilter<XMLConformanceTest>(DEFAULT_VERSION_FILTER, new TypeFilter(Type.VALID));
    
    public static final Filter<? super XMLConformanceTest> XERCES_2_9_1_FILTER = new ExclusionFilter<String>(new String[] {
            "valid-sa-097",
    });
}
