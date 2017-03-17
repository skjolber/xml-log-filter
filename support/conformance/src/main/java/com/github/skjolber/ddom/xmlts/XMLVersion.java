package com.github.skjolber.ddom.xmlts;

public enum XMLVersion {
    XML_1_0_EDITION_1("1.0", 1),
    XML_1_0_EDITION_2("1.0", 2),
    XML_1_0_EDITION_3("1.0", 3),
    XML_1_0_EDITION_4("1.0", 4),
    XML_1_0_EDITION_5("1.0", 5),
    XML_1_1("1.1", 1);
    
    private final String version;
	private final int edition;
	
	private XMLVersion(String version, int edition) {
		this.version = version;
		this.edition = edition;
	}
	
	public int getEdition() {
		return edition;
	}
	
	public String getVersion() {
		return version;
	}
	
}
