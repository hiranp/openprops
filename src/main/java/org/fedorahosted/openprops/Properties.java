/*
 * Copyright 1995-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
/*
 * Modified by Sean Flanigan <sflaniga@redhat.com> to preserve order and comments.
 */

package org.fedorahosted.openprops;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The <code>Properties</code> class represents a persistent set of
 * properties. The <code>Properties</code> can be saved to a stream
 * or loaded from a stream. Each key and its corresponding value in
 * the property list is a string.
 * <p>
 * A property list can contain another property list as its
 * "defaults"; this second property list is searched if
 * the property key is not found in the original property list.
 * <p>
 * Because <code>Properties</code> inherits from <code>Hashtable</code>, the
 * <code>put</code> and <code>putAll</code> methods can be applied to a
 * <code>Properties</code> object.  Their use is strongly discouraged as they
 * allow the caller to insert entries whose keys or values are not
 * <code>Strings</code>.  The <code>setProperty</code> method should be used
 * instead.  If the <code>store</code> or <code>save</code> method is called
 * on a "compromised" <code>Properties</code> object that contains a
 * non-<code>String</code> key or value, the call will fail. Similarly,
 * the call to the <code>propertyNames</code> or <code>list</code> method
 * will fail if it is called on a "compromised" <code>Properties</code>
 * object that contains a non-<code>String</code> key.
 *
 * <p>
 * The {@link #load(java.io.Reader) load(Reader)} <tt>/</tt>
 * {@link #store(java.io.Writer, java.lang.String) store(Writer, String)}
 * methods load and store properties from and to a character based stream
 * in a simple line-oriented format specified below.
 *
 * The {@link #load(java.io.InputStream) load(InputStream)} <tt>/</tt>
 * {@link #store(java.io.OutputStream, java.lang.String) store(OutputStream, String)}
 * methods work the same way as the load(Reader)/store(Writer, String) pair, except
 * the input/output stream is encoded in ISO 8859-1 character encoding.
 * Characters that cannot be directly represented in this encoding can be written using
 * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3">Unicode escapes</a>
 * ; only a single 'u' character is allowed in an escape
 * sequence. The native2ascii tool can be used to convert property files to and
 * from other character encodings.
 *
 * <p> The {@link #loadFromXML(InputStream)} and {@link
 * #storeToXML(OutputStream, String, String)} methods load and store properties
 * in a simple XML format.  By default the UTF-8 character encoding is used,
 * however a specific encoding may be specified if required.  An XML properties
 * document has the following DOCTYPE declaration:
 *
 * <pre>
 * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
 * </pre>
 * Note that the system URI (http://java.sun.com/dtd/properties.dtd) is
 * <i>not</i> accessed when exporting or importing properties; it merely
 * serves as a string to uniquely identify the DTD, which is:
 * <pre>
 *    &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *
 *    &lt;!-- DTD for properties --&gt;
 *
 *    &lt;!ELEMENT properties ( comment?, entry* ) &gt;
 *
 *    &lt;!ATTLIST properties version CDATA #FIXED "1.0"&gt;
 *
 *    &lt;!ELEMENT comment (#PCDATA) &gt;
 *
 *    &lt;!ELEMENT entry (#PCDATA) &gt;
 *
 *    &lt;!ATTLIST entry key CDATA #REQUIRED&gt;
 * </pre>
 *
 * @see <a href="../../../technotes/tools/solaris/native2ascii.html">native2ascii tool for Solaris</a>
 * @see <a href="../../../technotes/tools/windows/native2ascii.html">native2ascii tool for Windows</a>
 *
 * <p>This class is thread-safe: multiple threads can share a single
 * <tt>Properties</tt> object without the need for external synchronization.
 *
 * @author  Arthur van Hoff
 * @author  Michael McCloskey
 * @author  Xueming Shen
 * @since   JDK1.0
 */
@SuppressWarnings({"unchecked", "nls"})
public
class Properties {
    /** The serialVersionUID */
    private static final long serialVersionUID = 1L;

    // This should be pretty safe, at least for ISO-8859-1 files
    private static final String NEWLINE_REGEX = "(\r\n|\r|\n)"; //$NON-NLS-1$

    /**
     * Creates an empty property list with no default values.
     */
    public Properties() {
    }


    /**
     * Calls the <tt>Hashtable</tt> method <code>put</code>. Provided for
     * parallelism with the <tt>getProperty</tt> method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the <tt>Hashtable</tt> call to <code>put</code>.
     *
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @return     the previous value of the specified key in this property
     *             list, or <code>null</code> if it did not have one.
     * @see #getProperty
     * @since    1.2
     */
    public synchronized Object setProperty(String key, String value) {
        if (key == null || value == null)
            throw new NullPointerException();
        Entry entry = props.get(key);
        if (entry == null) {
            entry = new Entry("", value);
            props.put(key, entry);
            return null;
        } else {
            String result = entry.getValue();
            entry.setValue(value);
            return result;
        }
    }


    /**
     * Reads a property list (key and element pairs) from the input
     * character stream in a simple line-oriented format.
     * <p>
     * Properties are processed in terms of lines. There are two
     * kinds of line, <i>natural lines</i> and <i>logical lines</i>.
     * A natural line is defined as a line of
     * characters that is terminated either by a set of line terminator
     * characters (<code>\n</code> or <code>\r</code> or <code>\r\n</code>)
     * or by the end of the stream. A natural line may be either a blank line,
     * a comment line, or hold all or some of a key-element pair. A logical
     * line holds all the data of a key-element pair, which may be spread
     * out across several adjacent natural lines by escaping
     * the line terminator sequence with a backslash character
     * <code>\</code>.  Note that a comment line cannot be extended
     * in this manner; every natural line that is a comment must have
     * its own comment indicator, as described below. Lines are read from
     * input until the end of the stream is reached.
     *
     * <p>
     * A natural line that contains only white space characters is
     * considered blank and is ignored.  A comment line has an ASCII
     * <code>'#'</code> or <code>'!'</code> as its first non-white
     * space character; comment lines are also ignored and do not
     * encode key-element information.  In addition to line
     * terminators, this format considers the characters space
     * (<code>' '</code>, <code>'&#92;u0020'</code>), tab
     * (<code>'\t'</code>, <code>'&#92;u0009'</code>), and form feed
     * (<code>'\f'</code>, <code>'&#92;u000C'</code>) to be white
     * space.
     *
     * <p>
     * If a logical line is spread across several natural lines, the
     * backslash escaping the line terminator sequence, the line
     * terminator sequence, and any white space at the start of the
     * following line have no affect on the key or element values.
     * The remainder of the discussion of key and element parsing
     * (when loading) will assume all the characters constituting
     * the key and element appear on a single natural line after
     * line continuation characters have been removed.  Note that
     * it is <i>not</i> sufficient to only examine the character
     * preceding a line terminator sequence to decide if the line
     * terminator is escaped; there must be an odd number of
     * contiguous backslashes for the line terminator to be escaped.
     * Since the input is processed from left to right, a
     * non-zero even number of 2<i>n</i> contiguous backslashes
     * before a line terminator (or elsewhere) encodes <i>n</i>
     * backslashes after escape processing.
     *
     * <p>
     * The key contains all of the characters in the line starting
     * with the first non-white space character and up to, but not
     * including, the first unescaped <code>'='</code>,
     * <code>':'</code>, or white space character other than a line
     * terminator. All of these key termination characters may be
     * included in the key by escaping them with a preceding backslash
     * character; for example,<p>
     *
     * <code>\:\=</code><p>
     *
     * would be the two-character key <code>":="</code>.  Line
     * terminator characters can be included using <code>\r</code> and
     * <code>\n</code> escape sequences.  Any white space after the
     * key is skipped; if the first non-white space character after
     * the key is <code>'='</code> or <code>':'</code>, then it is
     * ignored and any white space characters after it are also
     * skipped.  All remaining characters on the line become part of
     * the associated element string; if there are no remaining
     * characters, the element is the empty string
     * <code>&quot;&quot;</code>.  Once the raw character sequences
     * constituting the key and element are identified, escape
     * processing is performed as described above.
     *
     * <p>
     * As an example, each of the following three lines specifies the key
     * <code>"Truth"</code> and the associated element value
     * <code>"Beauty"</code>:
     * <p>
     * <pre>
     * Truth = Beauty
     *  Truth:Beauty
     * Truth                    :Beauty
     * </pre>
     * As another example, the following three lines specify a single
     * property:
     * <p>
     * <pre>
     * fruits                           apple, banana, pear, \
     *                                  cantaloupe, watermelon, \
     *                                  kiwi, mango
     * </pre>
     * The key is <code>"fruits"</code> and the associated element is:
     * <p>
     * <pre>"apple, banana, pear, cantaloupe, watermelon, kiwi, mango"</pre>
     * Note that a space appears before each <code>\</code> so that a space
     * will appear after each comma in the final result; the <code>\</code>,
     * line terminator, and leading white space on the continuation line are
     * merely discarded and are <i>not</i> replaced by one or more other
     * characters.
     * <p>
     * As a third example, the line:
     * <p>
     * <pre>cheeses
     * </pre>
     * specifies that the key is <code>"cheeses"</code> and the associated
     * element is the empty string <code>""</code>.<p>
     * <p>
     *
     * <a name="unicodeescapes"></a>
     * Characters in keys and elements can be represented in escape
     * sequences similar to those used for character and string literals
     * (see <a
     * href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3">&sect;3.3</a>
     * and <a
     * href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.6">&sect;3.10.6</a>
     * of the <i>Java Language Specification</i>).
     *
     * The differences from the character escape sequences and Unicode
     * escapes used for characters and strings are:
     *
     * <ul>
     * <li> Octal escapes are not recognized.
     *
     * <li> The character sequence <code>\b</code> does <i>not</i>
     * represent a backspace character.
     *
     * <li> The method does not treat a backslash character,
     * <code>\</code>, before a non-valid escape character as an
     * error; the backslash is silently dropped.  For example, in a
     * Java string the sequence <code>"\z"</code> would cause a
     * compile time error.  In contrast, this method silently drops
     * the backslash.  Therefore, this method treats the two character
     * sequence <code>"\b"</code> as equivalent to the single
     * character <code>'b'</code>.
     *
     * <li> Escapes are not necessary for single and double quotes;
     * however, by the rule above, single and double quote characters
     * preceded by a backslash still yield single and double quote
     * characters, respectively.
     *
     * <li> Only a single 'u' character is allowed in a Uniocde escape
     * sequence.
     *
     * </ul>
     * <p>
     * The specified stream remains open after this method returns.
     *
     * @param   reader   the input character stream.
     * @throws  IOException  if an error occurred when reading from the
     *          input stream.
     * @throws  IllegalArgumentException if a malformed Unicode escape
     *          appears in the input.
     * @since   1.6
     */
    public synchronized void load(Reader reader) throws IOException {
        load0(new LineReader(reader));
    }

    /**
     * Reads a property list (key and element pairs) from the input
     * byte stream. The input stream is in a simple line-oriented
     * format as specified in
     * {@link #load(java.io.Reader) load(Reader)} and is assumed to use
     * the ISO 8859-1 character encoding; that is each byte is one Latin1
     * character. Characters not in Latin1, and certain special characters,
     * are represented in keys and elements using
     * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3">Unicode escapes</a>.
     * <p>
     * The specified stream remains open after this method returns.
     *
     * @param      inStream   the input stream.
     * @exception  IOException  if an error occurred when reading from the
     *             input stream.
     * @throws     IllegalArgumentException if the input stream contains a
     *             malformed Unicode escape sequence.
     * @since 1.2
     */
    public synchronized void load(InputStream inStream) throws IOException {
        load0(new LineReader(inStream));
    }

    private void load0 (LineReader lr) throws IOException {
        char[] convtBuf = new char[1024];
        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;
        StringBuilder prevComment = new StringBuilder();
        int lineNumber = lr.lineNumber;

        while ((limit = lr.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;
            //System.out.println("converted=<" + loadConvert(lr.lineBuf, 0, limit, convtBuf) + ">");
            //System.out.println("line=<" + new String(lr.lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                // looking for the end of the key...
                c = lr.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' ||  c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' ||  c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' &&  c != '\f') {
                    if (!hasSep && (c == '=' ||  c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            if (lr.isCommentLine) {
                prevComment.append(new String(lr.lineBuf, 0, limit));
            } else {
                String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
                String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
                props.put(key, new Entry(prevComment.toString(), value, lineNumber));
                prevComment.setLength(0);
            }
            lineNumber = lr.lineNumber;
        }
        footerComment = prevComment.toString();
    }

    /* Read in a "logical line" from an InputStream/Reader, skip all comment
     * and blank lines and filter out those leading whitespace characters
     * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
     * Method returns the char length of the "logical line" and stores
     * the line in "lineBuf".
     */
    class LineReader {
        int lineNumber = 1;

	public LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        public LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        byte[] inByteBuf;
        char[] inCharBuf;
        char[] lineBuf = new char[1024];
        int inLimit = 0;
        int inOff = 0;
        boolean isCommentLine;
        InputStream inStream;
        Reader reader;

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;
            
            while (true) {
                if (inOff >= inLimit) {
                    inLimit = (inStream==null)?reader.read(inCharBuf)
                                              :inStream.read(inByteBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0) {
                            return -1;
                        }
                        if (skipWhiteSpace) {
                            isCommentLine = true;
                        }
                        return len;
                    }
                }
                if (inStream != null) {
                    //The line below is equivalent to calling a
                    //ISO8859-1 decoder.
                    c = (char) (0xff & inByteBuf[inOff++]);
                } else {
                    c = inCharBuf[inOff++];
                }
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    // Leading whitespace is ignored by java.util.Properties,
                    // but we want to preserve whitespace before comments
                    if (c == ' ' || c == '\t' || c == '\f') {
                        // save the whitespace in case it precedes a comment
                        if (!appendedLineBegin)
                            lineBuf[len++] = c;
                        // skipping space/tab/formfeed
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        // return NL/CR as a comment line
                	isCommentLine = true;
                        lineBuf[len++] = c;
                        ++lineNumber;
                        return len;
                    }
                    // by this point, we've encountered (a) non-whitespace, or 
                    // (b) a newline which was not escaped by a backslash 
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        lineBuf[len++] = c;
                        skipWhiteSpace = false;
                        isCommentLine = true;
                        continue;
                    } else if (c == '\r' || c == '\n') {
                	// a blank line
                	// include newline in the comment
                	isCommentLine = true;
                        lineBuf[len++] = c;
                        ++lineNumber;
                        return len;

                    } else {
                        // Since this is not a comment line, drop the 
                        // whitespace we've been saving
                        len = 0;
                    }
                }

                if (c != '\n' && c != '\r') {
                    // an ordinary char (not leading whitespace or hash, and not EOL)
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        // expand lineBuf...
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    //flip the preceding backslash flag
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                }
                else {
                    // reached EOL
                    ++lineNumber;
                    if (isCommentLine || len == 0) {
                	// include newline in the comment
                        lineBuf[len++] = c;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        return len;
                    }
                    if (inOff >= inLimit) {
                        inLimit = (inStream==null)
                                  ?reader.read(inCharBuf)
                                  :inStream.read(inByteBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            return len;
                        }
                    } 
                    if (precedingBackslash) {
                        len -= 1;
                        //skip the leading whitespace characters in following line
                        // (unless they precede a comment)
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLF = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private String loadConvert (char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if(aChar == 'u') {
                    // Read the xxxx
                    int value=0;
                    for (int i=0; i<4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                          case '0': case '1': case '2': case '3': case '4':
                          case '5': case '6': case '7': case '8': case '9':
                             value = (value << 4) + aChar - '0';
                             break;
                          case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f':
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                          case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F':
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                          default:
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                        }
                     }
                    out[outLen++] = (char)value;
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String (out, 0, outLen);
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     */
    private String saveConvert(String theString,
                               boolean escapeSpace,
                               boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    private static void writeComments(BufferedWriter bw, String comments, boolean writeHashes)
        throws IOException {
        if (comments == null)
            return;
        if (writeHashes)
            bw.write("#");
        int len = comments.length();
        int current = 0;
        int last = 0;
        char[] uu = new char[6];
        uu[0] = '\\';
        uu[1] = 'u';
        while (current < len) {
            char c = comments.charAt(current);
            if (c > '\u00ff' || c == '\n' || c == '\r') {
                if (last != current)
                    bw.write(comments.substring(last, current));
                if (c > '\u00ff') {
                    uu[2] = toHex((c >> 12) & 0xf);
                    uu[3] = toHex((c >>  8) & 0xf);
                    uu[4] = toHex((c >>  4) & 0xf);
                    uu[5] = toHex( c        & 0xf);
                    bw.write(new String(uu));
                } else {
                    bw.newLine();
                    if (c == '\r' &&
                        current != len - 1 &&
                        comments.charAt(current + 1) == '\n') {
                        current++;
                    }
                    if (writeHashes && (current == len - 1 ||
                        (comments.charAt(current + 1) != '#' &&
                        comments.charAt(current + 1) != '!')))
                        bw.write("#");
                }
                last = current + 1;
            }
            current++;
        }
        if (last != current)
            bw.write(comments.substring(last, current));
        if (writeHashes)
        bw.newLine();
    }

    /**
     * Writes this property list (key and element pairs) in this
     * <code>Properties</code> table to the output character stream in a
     * format suitable for using the {@link #load(java.io.Reader) load(Reader)}
     * method.
     * <p>
     * Properties from the defaults table of this <code>Properties</code>
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * If the comments argument is not null, then an ASCII <code>#</code>
     * character, the comments string, and a line separator are first written
     * to the output stream. Thus, the <code>comments</code> can serve as an
     * identifying comment. Any one of a line feed ('\n'), a carriage
     * return ('\r'), or a carriage return followed immediately by a line feed
     * in comments is replaced by a line separator generated by the <code>Writer</code>
     * and if the next character in comments is not character <code>#</code> or
     * character <code>!</code> then an ASCII <code>#</code> is written out
     * after that line separator.
     * <p>
     * Next, a comment line is always written, consisting of an ASCII
     * <code>#</code> character, the current date and time (as if produced
     * by the <code>toString</code> method of <code>Date</code> for the
     * current time), and a line separator as generated by the <code>Writer</code>.
     * <p>
     * Then every entry in this <code>Properties</code> table is
     * written out, one per line. For each entry the key string is
     * written, then an ASCII <code>=</code>, then the associated
     * element string. For the key, all space characters are
     * written with a preceding <code>\</code> character.  For the
     * element, leading space characters, but not embedded or trailing
     * space characters, are written with a preceding <code>\</code>
     * character. The key and element characters <code>#</code>,
     * <code>!</code>, <code>=</code>, and <code>:</code> are written
     * with a preceding backslash to ensure that they are properly loaded.
     * <p>
     * After the entries have been written, the output stream is flushed.
     * The output stream remains open after this method returns.
     * <p>
     *
     * @param   writer      an output character stream writer.
     * @param   comments   a description of the property list.
     * @exception  IOException if writing this property list to the specified
     *             output stream throws an <tt>IOException</tt>.
     * @exception  ClassCastException  if this <code>Properties</code> object
     *             contains any keys or values that are not <code>Strings</code>.
     * @exception  NullPointerException  if <code>writer</code> is null.
     * @since 1.6
     */
    public void store(Writer writer, String comments)
        throws IOException
    {
        store0((writer instanceof BufferedWriter)?(BufferedWriter)writer
                                                 : new BufferedWriter(writer),
               comments,
               false);
    }

    /**
     * Writes this property list (key and element pairs) in this
     * <code>Properties</code> table to the output stream in a format suitable
     * for loading into a <code>Properties</code> table using the
     * {@link #load(InputStream) load(InputStream)} method.
     * <p>
     * Properties from the defaults table of this <code>Properties</code>
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * This method outputs the comments, properties keys and values in
     * the same format as specified in
     * {@link #store(java.io.Writer, java.lang.String) store(Writer)},
     * with the following differences:
     * <ul>
     * <li>The stream is written using the ISO 8859-1 character encoding.
     *
     * <li>Characters not in Latin-1 in the comments are written as
     * <code>&#92;u</code><i>xxxx</i> for their appropriate unicode
     * hexadecimal value <i>xxxx</i>.
     *
     * <li>Characters less than <code>&#92;u0020</code> and characters greater
     * than <code>&#92;u007E</code> in property keys or values are written
     * as <code>&#92;u</code><i>xxxx</i> for the appropriate hexadecimal
     * value <i>xxxx</i>.
     * </ul>
     * <p>
     * After the entries have been written, the output stream is flushed.
     * The output stream remains open after this method returns.
     * <p>
     * @param   out      an output stream.
     * @param   comments   a description of the property list.
     * @exception  IOException if writing this property list to the specified
     *             output stream throws an <tt>IOException</tt>.
     * @exception  ClassCastException  if this <code>Properties</code> object
     *             contains any keys or values that are not <code>Strings</code>.
     * @exception  NullPointerException  if <code>out</code> is null.
     * @since 1.2
     */
    public void store(OutputStream out, String comments)
        throws IOException
    {
        store0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")),
               comments,
               true);
    }

    private void store0(BufferedWriter bw, String comments, boolean escUnicode)
        throws IOException
    {
        if (comments != null)
            writeComments(bw, comments, true);
        synchronized (this) {
//            if (headerComment != null)
//                writeComments(bw, headerComment, false);
            for (String key : keySet()) {
                Entry entry = props.get(key);
                writeComments(bw, entry.getRawComment(), false);
                String val = entry.getValue();
                if (val == null) {
                    throw new NullPointerException("property key \""+key+"\" has a comment but no value");
                }
                key = saveConvert(key, true, escUnicode);
                /* No need to escape embedded and trailing spaces for value, hence
                 * pass false to flag.
                 */
                val = saveConvert(val, false, escUnicode);
                bw.write(key + "=" + val);
                bw.newLine();
            }
            writeComments(bw, footerComment, false);
        }
        bw.flush();
    }

    /**
     * Loads all of the properties represented by the XML document on the
     * specified input stream into this properties table.
     *
     * <p>The XML document must have the following DOCTYPE declaration:
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     * Furthermore, the document must satisfy the properties DTD described
     * above.
     *
     * <p>The specified stream is closed after this method returns.
     *
     * @param in the input stream from which to read the XML document.
     * @throws IOException if reading from the specified input stream
     *         results in an <tt>IOException</tt>.
     * @throws InvalidPropertiesFormatException Data on input stream does not
     *         constitute a valid XML document with the mandated document type.
     * @throws NullPointerException if <code>in</code> is null.
     * @see    #storeToXML(OutputStream, String, String)
     * @since 1.5
     */
    public synchronized void loadFromXML(InputStream in)
        throws IOException, InvalidPropertiesFormatException
    {
        if (in == null)
            throw new NullPointerException();
        XMLUtils.load(this, in);
        in.close();
    }

    /**
     * Emits an XML document representing all of the properties contained
     * in this table.
     *
     * <p> An invocation of this method of the form <tt>props.storeToXML(os,
     * comment)</tt> behaves in exactly the same way as the invocation
     * <tt>props.storeToXML(os, comment, "UTF-8");</tt>.
     *
     * @param os the output stream on which to emit the XML document.
     * @param comment a description of the property list, or <code>null</code>
     *        if no comment is desired.
     * @throws IOException if writing to the specified output stream
     *         results in an <tt>IOException</tt>.
     * @throws NullPointerException if <code>os</code> is null.
     * @throws ClassCastException  if this <code>Properties</code> object
     *         contains any keys or values that are not
     *         <code>Strings</code>.
     * @see    #loadFromXML(InputStream)
     * @since 1.5
     */
    public synchronized void storeToXML(OutputStream os, String comment)
        throws IOException
    {
        if (os == null)
            throw new NullPointerException();
        storeToXML(os, comment, "UTF-8");
    }

    /**
     * Emits an XML document representing all of the properties contained
     * in this table, using the specified encoding.
     *
     * <p>The XML document will have the following DOCTYPE declaration:
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
     * </pre>
     *
     *<p>If the specified comment is <code>null</code> then no comment
     * will be stored in the document.
     *
     * <p>The specified stream remains open after this method returns.
     *
     * @param os the output stream on which to emit the XML document.
     * @param comment a description of the property list, or <code>null</code>
     *        if no comment is desired.
     * @throws IOException if writing to the specified output stream
     *         results in an <tt>IOException</tt>.
     * @throws NullPointerException if <code>os</code> is <code>null</code>,
     *         or if <code>encoding</code> is <code>null</code>.
     * @throws ClassCastException  if this <code>Properties</code> object
     *         contains any keys or values that are not
     *         <code>Strings</code>.
     * @see    #loadFromXML(InputStream)
     * @since 1.5
     */
    public synchronized void storeToXML(OutputStream os, String comment,
                                       String encoding)
        throws IOException
    {
        if (os == null)
            throw new NullPointerException();
        XMLUtils.save(this, os, comment, encoding);
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns
     * <code>null</code> if the property is not found.
     *
     * @param   key   the property key.
     * @return  the value in this property list with the specified key value.
     * @see     #setProperty
     */
    public String getProperty(String key) {
        return get(key);
    }

    /**
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, the default property list,
     * and its defaults, recursively, are then checked. The method returns the
     * default value argument if the property is not found.
     *
     * @param   key            the hashtable key.
     * @param   defaultValue   a default value.
     *
     * @return  the value in this property list with the specified key value.
     * @see     #setProperty
     * @see     #defaults
     */
    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    /**
     * Returns an enumeration of all the keys in this property list,
     * including distinct keys in the default property list if a key
     * of the same name has not already been found from the main
     * properties list.
     *
     * @return  an enumeration of all the keys in this property list, including
     *          the keys in the default property list.
     * @throws  ClassCastException if any key in this property list
     *          is not a string.
     * @see     java.util.Enumeration
     * @see     java.util.Properties#defaults
     * @see     #stringPropertyNames
     */
    public Enumeration<?> propertyNames() {
        Hashtable h = new Hashtable();
        enumerate(h);
        return h.keys();
    }

    /**
     * Returns a set of keys in this property list where
     * the key and its corresponding value are strings,
     * including distinct keys in the default property list if a key
     * of the same name has not already been found from the main
     * properties list.  Properties whose key or value is not
     * of type <tt>String</tt> are omitted.
     * <p>
     * The returned set is not backed by the <tt>Properties</tt> object.
     * Changes to this <tt>Properties</tt> are not reflected in the set,
     * or vice versa.
     *
     * @return  a set of keys in this property list where
     *          the key and its corresponding value are strings,
     *          including the keys in the default property list.
     * @see     java.util.Properties#defaults
     * @since   1.6
     */
    public Set<String> stringPropertyNames() {
        return keySet();
    }

    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param   out   an output stream.
     * @throws  ClassCastException if any key in this property list
     *          is not a string.
     */
    public void list(PrintStream out) {
        out.println("-- listing properties --");
        Hashtable h = new Hashtable();
        enumerate(h);
        for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String val = (String)h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * Prints this property list out to the specified output stream.
     * This method is useful for debugging.
     *
     * @param   out   an output stream.
     * @throws  ClassCastException if any key in this property list
     *          is not a string.
     * @since   JDK1.1
     */
    /*
     * Rather than use an anonymous inner class to share common code, this
     * method is duplicated in order to ensure that a non-1.1 compiler can
     * compile this file.
     */
    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        Hashtable h = new Hashtable();
        enumerate(h);
        for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String val = (String)h.get(key);
            if (val.length() > 40) {
                val = val.substring(0, 37) + "...";
            }
            out.println(key + "=" + val);
        }
    }

    /**
     * Enumerates all key/value pairs in the specified hashtable.
     * @param h the hashtable
     * @throws ClassCastException if any of the property keys
     *         is not of String type.
     */
    private synchronized void enumerate(Hashtable h) {
        for (String key : keySet()) {
            h.put(key, get(key));
        }
    }

    /**
     * Convert a nibble to a hex character
     * @param   nibble  the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
    
    /** Including leading "#/!"s and one or more newlines */
//    private String headerComment = "";
    private String footerComment = "";

    private static final class Entry {
	/**
	 * Raw comment/whitespace, including comment marker and newlines
	 */
        private String rawComment;
        private String value;
        private int lineNumber;
        
        public Entry(String rawComment, String value, int lineNumber) {
            this.rawComment = rawComment;
            this.value = value;
            this.lineNumber = lineNumber;
        }
        
        public Entry(String rawComment, String value) {
            this(rawComment, value, -1);
        }
        
        public String getRawComment() {
            return rawComment;
        }
        
        public void setRawComment(String rawComment) {
            this.rawComment = rawComment;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public int getLineNumber() {
	    return lineNumber;
	}
        
        public void setLineNumber(int lineNumber) {
	    this.lineNumber = lineNumber;
	}
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
        	return true;
            if (!(obj instanceof Entry))
        	return false;
            Entry other = (Entry) obj;
            return Properties.equals(this.rawComment, other.rawComment) && Properties.equals(this.value, other.value);
//            return Properties.equals(this.rawComment, other.rawComment) && Properties.equals(this.value, other.value) &&
//            	this.lineNumber == other.lineNumber;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (rawComment == null ? 0 : rawComment.hashCode());
            hash = 31 * hash + (value == null ? 0 : value.hashCode());
            return hash;
        }

        @Override
        public String toString() {
            return "Entry[comment="+rawComment+" value="+value+"]";
        }
    }
    
    private final Map<String,Entry> props = new LinkedHashMap<String, Entry>();

    static final String PROJECT_NAME = "OpenProperties";

    private String get(String key) {
        Entry entry = props.get(key);
        return entry == null ? null : entry.getValue();
    }

    /**
     * Returns the set of property keys.
     * 
     * @return
     */
    public Set<String> keySet() {
        return props.keySet();
    }
    
    public String getFooterComment() {
	return cookComment(footerComment);
    }
    
    public void setFooterComment(String comment) {
	this.footerComment = uncookComment(comment);
    }
    
    public String getRawFooterComment() {
	return footerComment;
    }
    
    public void setRawFooterComment(String comment) {
	this.footerComment = comment;
    }
    
    /**
     * Returns the "raw" comment for the specified key, or null if there is none.
     * Note that the raw comment may include empty lines or lines which 
     * contain only whitespace, and non-whitespace lines will include the 
     * comment marker ("#" or "!"). 
     * 
     * @param key
     * @return
     */
    public String getRawComment(String key) {
        Entry entry = props.get(key);
        return entry == null ? null : entry.getRawComment();
    }
    
    /**
     * Sets the "raw" comment for the specified key.  Each line of the 
     * comment must be either empty, whitespace-only, or preceded by a 
     * comment marker ("#" or "!").  This is not enforced by this class.
     * <br>
     * Note: if you set a comment, you must set a corresponding value before 
     * calling store or storeToXML. 
     * 
     * @param key property key whose comment should be set
     * @param rawComment raw comment to go with property key, "" for no comment
     */
    public void setRawComment(String key, String rawComment) {
	if (rawComment == null)
	    throw new NullPointerException();
        Entry entry = props.get(key);
        if (entry == null) {
            entry = new Entry(rawComment, null);
            props.put(key, entry);
        } else {
            entry.setRawComment(rawComment);
        }
    }

    /**
     * Sets the comment for the specified key.  Each line of the comment 
     * will be preceded by the comment marker "#", and leading and 
     * trailing whitespace will not be preserved.
     * <br>
     * Note: if you set a comment, you must set a corresponding value before 
     * calling store or storeToXML. 
     * 
     * @param key property key whose comment should be set
     * @param comment comment to go with property key, "" for no comment
     */
    public void setComment(String key, String comment) {
	if (comment == null)
	    throw new NullPointerException();
        setRawComment(key, uncookComment(comment));
    }
    
    /**
     * Returns the comment for the specified key, or null if there is none.
     * Any embedded newline sequences will be replaced by \n characters.
     * @param key
     * @return
     */
    public String getComment(String key) {
        String raw = getRawComment(key);
        return cookComment(raw);
    }
    
    public int getLineNumber(String key) {
	return props.get(key).lineNumber;
    }
    
    public synchronized int size() {
	return props.size();
    }
    
    private static String cookComment(String raw) {
        if (raw == null)
            return null;
        StringBuilder sb = new StringBuilder(raw.length());
        
        String[] lines = raw.split(NEWLINE_REGEX);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            sb.append(cookCommentLine(line));
            if(i+1 < lines.length)
                sb.append('\n');
        }
        return sb.toString();
    }
    
    private static String uncookComment(String comment) {
        if (comment.length() != 0) {
            StringBuilder sb = new StringBuilder(comment.length()+30);
            String[] lines = comment.split(NEWLINE_REGEX);
            for (String line : lines) {
        	// NB writeComments() will replace \n with a BufferedWriter.newLine() call
                sb.append("# ").append(line).append('\n'); //$NON-NLS-1$
            }
            return sb.toString();
        }
        return ""; //$NON-NLS-1$
    }
    
    public static String cookCommentLine(String line) {
        // remove leading whitespace (and \r)
        String trimmed = line.trim();
        if (trimmed.length() != 0) {
            switch (trimmed.charAt(0)) {
            case '#':
            case '!':
                // remove comment-marker and any following whitespace
                return trimmed.substring(1).trim();
            default:
                return trimmed;
            }
        }
        return ""; //$NON-NLS-1$
    }
    
    private static boolean equals(Object a, Object b) {
	if (a == null)
	    return b == null;
	return a.equals(b);
    }
    
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
	    return false;
        Properties other = (Properties) obj;
        return this.footerComment.equals(other.footerComment) && this.props.equals(other.props);
    }
    
    @Override
    public int hashCode() {
	int hash = 7;
	hash = 31 * hash + footerComment.hashCode();
	hash = 31 * hash + props.hashCode();
	return hash;
    }
    
    @Override
    public String toString() {
	StringWriter writer = new StringWriter();
	try {
	    store(writer, null);
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	return getClass().getName()+"["+writer.toString()+"]";
    }

}
