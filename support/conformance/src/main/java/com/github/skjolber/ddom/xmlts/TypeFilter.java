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

import java.util.EnumSet;
import java.util.Set;

import com.github.skjolber.ddom.collections.Filter;
import com.github.skjolber.ddom.xmlts.XMLConformanceTest.Type;

public class TypeFilter implements Filter<XMLConformanceTest> {
	
    private final Set<Type> types;
    
    public TypeFilter(Type type) {
        types = EnumSet.of(type);
    }
    
    public TypeFilter(Type first, Type... rest) {
        types = EnumSet.of(first, rest);
    }

    public boolean accept(XMLConformanceTest test) {
        return types.contains(test.getType());
    }
}
