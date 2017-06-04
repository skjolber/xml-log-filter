package com.github.skjolber.xmlfilter.servlet.example.io;

public class CharArrayReader extends java.io.CharArrayReader {

	private int offset; 
	private int length;
	
	public CharArrayReader(char[] buf) {
		this(buf, 0, buf.length);
	}

	public CharArrayReader(char[] buf, int offset, int length) {
		super(buf, offset, length);
		
		this.offset = offset;
		this.length = length;
	}

	public char[] getCharArray() {
		return buf;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getOffset() {
		return offset;
	}
}
