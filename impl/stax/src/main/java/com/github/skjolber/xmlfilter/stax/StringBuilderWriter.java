package com.github.skjolber.xmlfilter.stax;

/*
 * Copyright 1996-2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

import java.io.IOException;
import java.io.Writer;


/**
 * A character stream that collects its output in a string buffer, which can
 * then be used to construct a string.
 * <p>
 * Closing a StringWriter has no effect. The methods in this class
 * can be called after the stream has been closed without generating an
 * IOException.
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */

public class StringBuilderWriter extends Writer {

    private StringBuilder buf;

    /**
     * Create a new string writer using the default initial string-buffer
     * size.
     */
    public StringBuilderWriter(StringBuilder stringBuffer) {
    	this.buf = stringBuffer;
    }

    /**
     * Write a single character.
     */
    public void write(int c) {
        buf.append((char) c);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     */
    public void write(char cbuf[], int off, int len) {
    	if(cbuf == null) {
    		throw new NullPointerException();
    	}
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buf.append(cbuf, off, len);
    }

    /**
     * Write a string.
     */
    public void write(String str) {
        buf.append(str);
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  String to be written
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     */
    public void write(String str, int off, int len)  {
        buf.append(str.substring(off, off + len));
    }

    /**
     * Appends the specified character sequence to this writer.
     *
     * <p> An invocation of this method of the form out.append(csq)
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> Depending on the specification of toString for the
     * character sequence csq, the entire sequence may not be
     * appended. For instance, invoking the toString method of a
     * character buffer will return a subsequence whose content depends upon
     * the buffer's position and limit.
     *
     * @param  csq
     *         The character sequence to append.  If csq is
     *         null, then the four characters "null" are
     *         appended to this writer.
     *
     * @return  This writer
     *
     * @since  1.5
     */
    public StringBuilderWriter append(CharSequence csq) {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this writer.
     *
     * <p> An invocation of this method of the form out.append(csq, start,
     * end) when csq is not null, behaves in
     * exactly the same way as the invocation
     *
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * @param  csq
     *         The character sequence from which a subsequence will be
     *         appended.  If csq is null, then characters
     *         will be appended as if csq contained the four
     *         characters "null".
     *
     * @param  start
     *         The index of the first character in the subsequence
     *
     * @param  end
     *         The index of the character following the last character in the
     *         subsequence
     *
     * @return  This writer
     *
     * @throws  IndexOutOfBoundsException
     *          If start or end are negative, start
     *          is greater than end, or end is greater than
     *          csq.length()
     *
     * @since  1.5
     */
    public StringBuilderWriter append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * Appends the specified character to this writer.
     *
     * <p> An invocation of this method of the form out.append(c)
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     out.write(c) </pre>
     *
     * @param  c
     *         The 16-bit character to append
     *
     * @return  This writer
     *
     * @since 1.5
     */
    public StringBuilderWriter append(char c) {
        write(c);
        return this;
    }

    /**
     * Return the buffer's current value as a string.
     */
    public String toString() {
        return buf.toString();
    }

    /**
     * Return the string buffer itself.
     *
     * @return StringBuffer holding the current buffer value.
     */
    public StringBuilder getBuffer() {
        return buf;
    }

    /**
     * Flush the stream.
     */
    public void flush() {
    }

    /**
     * Closing a StringWriter has no effect. The methods in this
     * class can be called after the stream has been closed without generating
     * an IOException.
     */
    public void close() throws IOException {
    }

}