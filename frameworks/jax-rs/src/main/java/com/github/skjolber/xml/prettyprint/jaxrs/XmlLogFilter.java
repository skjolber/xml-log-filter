package com.github.skjolber.xml.prettyprint.jaxrs;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@NameBinding
public @interface XmlLogFilter {
	
	boolean indent() default false;

	boolean xmlDeclaration() default false;

	int maxTextNodeLength() default -1;
	int maxCDATANodeLength() default -1;
	
	String[] anonymizeFilters() default {};
	String[] pruneFilters() default {};

}