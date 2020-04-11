package com.skjolber.github.modules.example;

public class JsonLogFilter {

	public enum Type {
		/** public for testing */
		KEY(), VALUE(), NEITHER();
		
		private Type() {
		}
	}
	
	public boolean process(char[] chars, int offset, int length, StringBuilder output) {

		int bufferLength = output.length();
		// array, boolean, null, number, object and string

		// {, }, [, ], : and ,

		length += offset;

		int sourceStart = offset;

		int level = 0;
		
		Type type = Type.NEITHER;

		try {
			while(offset < length) {

				do {
					if(Character.isWhitespace(chars[offset++])) {
						break;
					}
				} while(offset < length);
				if(offset < length) {				
				
					switch(chars[offset]) {
					case '{' : {
						type = Type.KEY;
						
						level++;
						break;
					}
					
					case '}' : {
						type = Type.NEITHER;
						
						level--;
						break;
					}
					
					case '[' : {
						type = Type.NEITHER;
						
						level++;
						break;
					}
					
					case ']' : {
						type = Type.NEITHER;
						
						level--;
						break;
					}
					
					case ',' : {
						type = Type.KEY;
						break;
					}
					
					case ':' : {
						type = Type.VALUE;
						
						break;
					}
					
					case '"' : {
						offset = scanBeyondKeyName(chars, offset, length);
						
						break;
					}
	
					}
				}

			}
			if(level != 0) {
				output.setLength(bufferLength);
				
				return false;
			}

			if(sourceStart < offset) {
				output.append(chars, sourceStart, offset - sourceStart);
				sourceStart = offset;
			}
		} catch(Exception e) {
			output.setLength(bufferLength);
			
			return false;
		}
		
		return true;
	}

	public static final int scanBeyondKeyName(final char[] chars, int offset, int limit) {
		while(offset < limit) {
			if(chars[offset++] == '"') {
				return offset;
			}
		}
		throw new ArrayIndexOutOfBoundsException("Unable to find end of key name");
	}	
}
