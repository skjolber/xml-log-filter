package com.skjolberg.xmlfilter.filter;

public class CharArrayFilter {
	
	protected static final int DELTA_ARRAY_SIZE = 8;
	
	public static final int FILTER_PRUNE = 0;
	public static final int FILTER_ANON = 1;
	public static final int FILTER_MAX_LENGTH = 2;
	
	public static final String FILTER_PRUNE_MESSAGE = " [SUBTREE REMOVED] ";
	public static final String FILTER_PRUNE_MESSAGE_COMMENT = "<!--" + FILTER_PRUNE_MESSAGE + "-->";
	public static final String FILTER_ANONYMIZE_MESSAGE = "[*****]";
	public static final String FILTER_TRUNCATE_MESSAGE = "...[TRUNCATED BY ";

	public static final char[] FILTER_PRUNE_MESSAGE_CHARS = FILTER_PRUNE_MESSAGE_COMMENT.toCharArray();
	public static final char[] FILTER_ANONYMIZE_MESSAGE_CHARS = FILTER_ANONYMIZE_MESSAGE.toCharArray();
	public static final char[] FILTER_TRUNCATE_MESSAGE_CHARS = FILTER_TRUNCATE_MESSAGE.toCharArray();

	private int[] filter;
	private int filterIndex = 0;

	public CharArrayFilter() {
		 filter = new int[DELTA_ARRAY_SIZE * 3];
	}

	public void add(int start, int end, int type) {
		if(filter.length <= filterIndex) {
			
			int[] next = new int[filter.length + 3 * 4];
			System.arraycopy(filter, 0, next, 0, filter.length);
			
			filter = next;
		}

		filter[filterIndex++] = start;
		filter[filterIndex++] = end;
		filter[filterIndex++] = type;
	}

	public int getFilterIndex() {
		return filterIndex;
	}
	
	public void filter(final char[] chars, int offset, int length, final StringBuilder buffer) {
		length += offset;
		
		for(int i = 0; i < filterIndex; i+=3) {
			buffer.append(chars, offset, filter[i] - offset);
			
			if(filter[i+2] == FILTER_ANON) {
				buffer.append(FILTER_ANONYMIZE_MESSAGE_CHARS);
			} else if(filter[i+2] == FILTER_PRUNE) {
				buffer.append(FILTER_PRUNE_MESSAGE_CHARS);
			} else {
				buffer.append(FILTER_TRUNCATE_MESSAGE_CHARS);
				buffer.append(-filter[i+2]);
				buffer.append(']');
			}
			offset = filter[i + 1];
		}
		
		if(offset < length) {
			buffer.append(chars, offset, length - offset);
		}
	}
	
	public static boolean isIndentationWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}

	/**
	 * 
	 * Scan from start element start to start element end, plus one.
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset  one character past the start tag
	 * @throws ArrayIndexOutOfBoundsException if limit has been reached
	 * 
	 */

	public static final int scanBeyondStartElement(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset++] == '>') {
				return offset;
			}
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end");
	}
	
	public final static int scanBeyondDTDEnd(final char[] chars, int offset, int limit) {
		// assume DTD are nested structures
		// simplified scan loop
		int level = 1;

		while(offset < limit) {
			if(chars[offset] == '<') {
				if(chars[offset + 1] == '!') {
					if(chars[offset + 2] == '-') {
						// comment
						offset = scanBeyondCommentEnd(chars, offset + 2, limit);
					} else {
						level++;
					}
				}
			} else if(chars[offset] == '>') {
				level--;
			} else if(chars[offset] == '"') {
				// scan through next "
				do {
					offset++;
				} while(chars[offset] != '"' && offset < limit);
			} else if(chars[offset] == '\'') {
				// scan through next "
				do {
					offset++;
				} while(chars[offset] != '\'' && offset < limit);
				
			}
			offset++;
			
			if(level == 0) {
				return offset;
			}
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end");
	}

	public final static int scanProcessingInstructionEnd(final char[] chars, int offset, int limit) {
		do {
			offset = scanBeyondStartElement(chars, offset, limit);
			if(chars[offset - 2] == '?') {
				return offset;
			}
		} while(true);
	}
	

	/**
	 * 
	 * Scan one past CDATA end
	 * 
	 * @param chars XML data
	 * @param offset start offset within XML data
	 * @param limit end offset within XML data
	 * 
	 * @return offset one character past the CDATA end
	 * 
	 */
	
	public static int scanBeyondCDataEnd(final char[] chars, int offset, int limit) {
		return scanBeyondEnd(chars, offset, limit, ']');
	}

	public static int scanBeyondCommentEnd(final char[] chars, int offset, int limit) {
		return scanBeyondEnd(chars, offset, limit, '-');
	}
	
	private static int scanBeyondEnd(final char[] chars, int offset, int limit, char a) {
		do {
			offset = scanBeyondStartElement(chars, offset, limit);
			if(chars[offset - 2] == a && chars[offset - 3] == a) {
				return offset;
			}
		} while(true);
	}
	
	public static int skipSubtree(final char[] chars, int offset, int limit) {
		
		int level = 0;
		
		while(offset < limit) {
			
			if(chars[offset] == '<') {
				switch(chars[offset + 1]) {
					case '/' : {  // end tag
						level--;
	
						if(level < 0) {
							return offset;
						}
						
						offset = scanBeyondStartElement(chars, offset + 3, limit);
						
						continue;
					}
					case '!': {
						// complete cdata and comments so nodes
						
						if(chars[offset + 2] == '-') {
							// look for -->
							offset = scanBeyondCommentEnd(chars, offset + 3, limit);
							
							continue;
						} else if(chars[offset + 2] == '[') {
							// look for ]]>
							offset = scanBeyondCDataEnd(chars, offset + 11, limit);
							
							continue;
						} else {
							// do nothing
						}
						break;
					}
					case '?' : {
						// processing instruction
						offset = scanProcessingInstructionEnd(chars, offset + 3, limit);
						
						continue;
					} 
					default : {
						// start element
						// flush bytes
						level++;
	
						// scan to end of start element to see if empty element
						offset += 2; // skip <a in <a>
						while(offset < limit) {
							if(chars[offset] == '>') {
								if(chars[offset - 1] == '/') {
									// empty element
									level--;
								}
								
								offset++;
								
								break;
							}
							offset++;
						}
						
						continue;
					}
				}	
			}
			
			offset++;
		}
		
		return offset;

	}

	public static boolean startsWithXMLDeclaration(final char[] chars, int sourceStart, int sourceEnd) {
		return sourceStart < sourceEnd - 6 && chars[sourceStart + 2] == 'x' && chars[sourceStart + 3] == 'm' && chars[sourceStart + 4] == 'l' && isIndentationWhitespace(chars[sourceStart + 5]);
		// assume method is inlined 
	}

	public static boolean matchRegion(final char[] chars, int start, int end, final char[] attribute) {
		// check if wildcard
		if(attribute.length == end - start) {
			for(int i = 0; i < attribute.length; i++) {
				if(attribute[i] != chars[start + i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
			
}
