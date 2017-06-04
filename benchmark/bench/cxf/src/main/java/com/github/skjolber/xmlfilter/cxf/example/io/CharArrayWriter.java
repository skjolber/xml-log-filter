package com.github.skjolber.xmlfilter.cxf.example.io;

public class CharArrayWriter extends java.io.CharArrayWriter {

	public CharArrayWriter() {
		super();
	}

	public CharArrayWriter(int initialSize) {
		super(initialSize);
	}

	public char[] getCharArray() {
		return buf;
	}
	
	
}
