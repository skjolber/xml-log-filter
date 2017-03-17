package com.github.skjolber.xmlfilter.test;

public interface XmlFilterConstants {

	public static final String INVALID_XPATH = "/no[1]/match";
	public static final String PASSTHROUGH_XPATH = "/no/match";
	public static final String DEFAULT_XPATH = "/aparent/achild";
	public static final String DEFAULT_WILDCARD_XPATH = "/aparent/*";
	public static final String DEFAULT_ATTRIBUTE_XPATH = "/aparent/achild/@attr";
	public static final String DEFAULT_ATTRIBUTE_WILDCARD_XPATH = "/aparent/achild/@*";

	public static final int DEFAULT_MAX_LENGTH = 127;

	public static final String DEFAULT_ANY_XPATH = "//achild";
	
	public static final String BASE_PATH = "./../../support/test/src/main/resources/xml/";

}
