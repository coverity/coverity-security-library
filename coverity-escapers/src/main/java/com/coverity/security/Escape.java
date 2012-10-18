/**
 *   Copyright (c) 2012, Coverity, Inc. 
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without modification, 
 *   are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice, this 
 *   list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *   - Neither the name of Coverity, Inc. nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific prior 
 *   written permission from Coverity, Inc.
 *   
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *   EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND INFRINGEMENT ARE DISCLAIMED.
 *   IN NO EVENT SHALL THE COPYRIGHT HOLDER OR  CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *   INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 *   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 *   WHETHER IN CONTRACT,  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 *   OF SUCH DAMAGE.
 */
package com.coverity.security;

/**
 * Escape is a small set of methods for escaping tainted data. These escaping
 * methods are useful in transforming user-controlled ("tainted") data into
 * forms that are safe from being interpreted as something other than data, such
 * as JavaScript.
 * <p>
 * At this time most of these escaping routines focus on cross-site scripting
 * mitigations. Each method is good for a different HTML context. For a primer
 * on HTML contexts, see OWASP's XSS Prevention Cheat Sheet (note however that
 * the escaping routines are not implemented exactly according to OWASP's
 * recommendations) or the Coverity Security Advisor documentation. 
 * Also see the Coverity Security Research Laboratory blog on
 * how to properly use each function.
 * <p>
 * While Coverity's static analysis product references these escaping routines
 * as exemplars and understands their behavior, there is no dependency on
 * Coverity products and these routines are completely standalone. Feel free to
 * use them! Just make sure you use them correctly.
 * 
 * @author Romain Gaucher
 * @author Andy Chou
 * @author Jon Passki
 * 
 */
public class Escape {

    /**
     * HTML entity escaping for text content and attributes.
     * <p>
     * HTML entity escaping that is appropriate for the most common HTML contexts:
     * PCDATA and "normal" attributes (non-URI, non-event, and non-CSS attributes). <br />
     * Note that we do not recommend using non-quoted HTML attributes since
     * the security obligations vary more between web browser. We recommend
     * to always quote (single or double quotes) HTML attributes.<br />
     * This method is generic to HTML entity escaping, and therefore escapes more
     * characters than usually necessary -- mostly to handle non-quoted attribute values.
     * If this method is somehow too slow, such as you output megabytes of text with spaces,
     * please use the {@link #htmlText(String)} method which only escape HTML text specific
     * characters.
     *
     * <p>
     * The following characters are escaped:
     * <ul>
     * <li>
     * HTML characters: <code>' (U+0022)</code>, <code>" (U+0027)</code>, 
     *                  <code>\ (U+005C)</code>, <code>/ (U+002F)</code>, 
     *                  <code>&lt; (U+003C)</code>, <code>&gt; (U+003E)</code>, 
     *                  <code>&amp; (U+0026)</code>
     * </li>
     * <li>
     * Control characters: <code>\t (U+0009)</code>, <code>\n (U+000A)</code>, 
     *                     <code>\f (U+000C)</code>, <code>\r (U+000D)</code>, 
     *                     <code>SPACE (U+0020)</code>
     * </li>
     * <li>
     * Unicode newlines: <code>LS (U+2028)</code>, <code>PS (U+2029)</code>
     * </li>
     * </ul>
     *
     * @param  input the string to be escaped
     * @return       the HTML escaped string or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String html(String input) {
        if (input == null)
            return null;

        int length = input.length();
        StringBuilder output = allocateStringBuilder(input, length);
        
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            switch (c) {
            // Control chars
            case '\t':
                output.append("&#x09;");
                break;
            case '\n':
                output.append("&#x0A;");
                break;
            case '\f':
                output.append("&#x0C;");
                break;
            case '\r':
                output.append("&#x0D;");
                break;
            // Chars that have a meaning for HTML
            case '\'':
                output.append("&#39;");
                break;
            case '\\':
                output.append("&#x5C;");
                break;
            case ' ':
                output.append("&#x20;");
                break;
            case '/':
                output.append("&#x2F;");
                break;
            case '"':
                output.append("&quot;");
                break;
            case '<':
                output.append("&lt;");
                break;
            case '>':
                output.append("&gt;");
                break;
            case '&':
                output.append("&amp;");
                break;
            // Unicode new lines
            case '\u2028':
                output.append("&#x2028;");
                break;
            case '\u2029':
                output.append("&#x2029;");
                break;

            default:
                output.append(c);
                break;
            }
        }
        return output.toString();
    }


    /**
     * Faster HTML entity escaping for tag content or quoted attributes values only.
     * <p>
     * HTML entity escaping that is specific to text elements such as the content of
     * a typical HTML tag (<code>div</code>, <code>p</code>, etc.).<br />
     * This method is not appropriate in all cases, and especially when appending data
     * in a non-quoted context (e.g., an HTML attribute value that is not surrounded by
     * single or double quotes). Note that we however, highly discourage the use 
     * of non-quoted attributes.
     *
     * <p>
     * The following characters are escaped:
     * <ul>
     * <li>
     * HTML characters: <code>' (U+0022)</code>, <code>" (U+0027)</code>,  
     *                  <code>&lt; (U+003C)</code>, <code>&gt; (U+003E)</code>,  
     *                  <code>&amp; (U+0026)</code>
     * </li>
     * </ul>
     *
     * @param  input the string to be escaped
     * @return       the HTML escaped string or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String htmlText(String input) {
        if (input == null)
            return null;

        int length = input.length();
        StringBuilder output = allocateStringBuilder(input, length);
        
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            switch (c) {
            case '\'':
                output.append("&#39;");
                break;
            case '"':
                output.append("&quot;");
                break;
            case '<':
                output.append("&lt;");
                break;
            case '>':
                output.append("&gt;");
                break;
            case '&':
                output.append("&amp;");
                break;
            default:
                output.append(c);
                break;
            }
        }
        return output.toString();   
    }


    /**
     * URI encoder.
     * <p>
     * URI encoding for query string values of the URI: 
     *  <code>/example/?name=URI_ENCODED_VALUE_HERE</code> <br />
     * Note that this method is not sufficient to protect for cross-site scripting
     * in a generic URI context, but only for query string values. If you
     * need to escape a URI in an <code>href</code> attribute (for example), 
     * ensure that:
     * <ul>
     *   <li>The scheme is allowed (restrict to http, https, or mailto)</li>
     *   <li>Use the HTML escaper {@link #html(String)} on the entire URI</li>
     * </ul>
     * <p>
     * This URI encoder processes the following characters:
     * <ul>
     * <li>
     * URI characters: <code>' (U+0022)</code>, <code>" (U+0027)</code>, 
     *                 <code>\ (U+005C)</code>, <code>/ (U+002F)</code>, 
     *                 <code>&lt; (U+003C)</code>, <code>&gt; (U+003E)</code>,  
     *                 <code>&amp; (U+0026)</code>, 
     *                 <code>&lt; (U+003C)</code>, <code>&gt; (U+003E)</code>, 
     *                 <code>! (U+0021)</code>, <code># (U+0023)</code>, 
     *                 <code>$ (U+0024)</code>, <code>% (U+0025)</code>, 
     *                 <code>( (U+0028)</code>, <code>) (U+0029)</code>, 
     *                 <code>* (U+002A)</code>, <code>+ (U+002B)</code>, 
     *                 <code>, (U+002C)</code>, <code>. (U+002E)</code>, 
     *                 <code>: (U+003A)</code>, <code>; (U+003B)</code>, 
     *                 <code>= (U+003D)</code>, <code>? (U+003F)</code>, 
     *                 <code>@ (U+0040)</code>, <code>[ (U+005B)</code>, 
     *                 <code>] (U+005D)</code> 
     * </li>
     * <li>
     * Control characters: <code>\t (U+0009)</code>, <code>\n (U+000A)</code>, 
     *                     <code>\f (U+000C)</code>, <code>\r (U+000D)</code>, 
     *                     <code>SPACE (U+0020)</code>
     * </li>
     * </ul>
     *
     * @param  input the string to be escaped
     * @return       the URI encoded string or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String uriParam(String input) {
        if (input == null)
            return null;

        int length = input.length();
        StringBuilder output = allocateStringBuilder(input, length);

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            switch (c) {
            // Control chars
            case '\t':
                output.append("%09");
                break;
            case '\n':
                output.append("%0A");
                break;
            case '\f':
                output.append("%0C");
                break;
            case '\r':
                output.append("%0D");
                break;
            // RFC chars to encode, plus % ' " < and >, and space
            case ' ':
                output.append("%20");
                break;
            case '!':
                output.append("%21");
                break;
            case '"':
                output.append("%22");
                break;
            case '#':
                output.append("%23");
                break;
            case '$':
                output.append("%24");
                break;
            case '%':
                output.append("%25");
                break;
            case '&':
                output.append("%26");
                break;
            case '\'':
                output.append("%27");
                break;
            case '(':
                output.append("%28");
                break;
            case ')':
                output.append("%29");
                break;
            case '*':
                output.append("%2A");
                break;
            case '+':
                output.append("%2B");
                break;
            case ',':
                output.append("%2C");
                break;
            case '.':
                output.append("%2E");
                break;
            case '/':
                output.append("%2F");
                break;
            case ':':
                output.append("%3A");
                break;
            case ';':
                output.append("%3B");
                break;
            case '<':
                output.append("%3C");
                break;
            case '=':
                output.append("%3D");
                break;
            case '>':
                output.append("%3E");
                break;
            case '?':
                output.append("%3F");
                break;
            case '@':
                output.append("%40");
                break;
            case '[':
                output.append("%5B");
                break;
            case ']':
                output.append("%5D");
                break;

            default:
                output.append(c);
                break;
            }
        }
        return output.toString();
    }


    /**
     * Same as {@link #uriParam(String)} for now.
     * <p>
     * Eventually, this method will evolve into filtering the URI so that
     * it is safely considered as a URL by a web browser, and does not contain
     * malicious payloads (data:text/html..., javascript:, etc.).
     */
    public static String uri(String input) {
        return uriParam(input);
    }


    /**
     * JavaScript String Unicode escaper.
     * <p>
     * JavaScript String Unicode escaping (<code>\UXXXX</code>) to be used in single or double quoted
     * JavaScript strings: 
     * <pre>
     * &lt;script type="text/javascript"&gt;
     *   window.myString = 'JS_STRING_ESCAPE_HERE';
     *   window.yourString = "JS_STRING_ESCAPE_HERE";
     * &lt;/script&gt;
     * </pre>
     * <p>
     * This JavaScript string escaper processes the following characters:
     * <ul>
     * <li>
     * JS String characters: <code>' (U+0022)</code>, <code>" (U+0027)</code>, 
     *                       <code>\ (U+005C)</code> 
     * </li>
     * <li>
     * HTML characters: <code>/ (U+002F)</code>,
     *                  <code>&lt; (U+003C)</code>, <code>&gt; (U+003E)</code>, 
     *                  <code>&amp; (U+0026)</code>
     * </li>
     * <li>
     * Control characters: <code>\b (U+0008)</code>, <code>\t (U+0009)</code>, 
     *                     <code>\n (U+000A)</code>, <code>0x0b (U+000B)</code>, 
     *                     <code>\f (U+000C)</code>, <code>\r (U+000D)</code> 
     * </li>
     * <li>
     * Unicode newlines: <code>LS (U+2028)</code>, <code>PS (U+2029)</code> 
     * </li>
     * </ul>
     *
     * @param  input the string to be escaped
     * @return       the JavaScript string Unicode escaped string or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String jsString(String input) {
        if (input == null)
            return null;

        int length = input.length();
        StringBuilder output = allocateStringBuilder(input, length);
        
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            switch (c) {
            // Control chars
            case '\b':
                output.append("\\u0008");
                break;
            case '\t':
                output.append("\\u0009");
                break;
            case '\n':
                output.append("\\u000A");
                break;
            case '\u000b':
                output.append("\\u000B");
                break;
            case '\f':
                output.append("\\u000C");
                break;
            case '\r':
                output.append("\\u000D");
                break;
            // JavaScript String chars
            case '\'':
                output.append("\\u0027");
                break;
            case '"':
                output.append("\\u0022");
                break;
            case '\\':
                output.append("\\u005C");
                break;
            // HTML chars for closing the parent context
            case '&':
                output.append("\\u0026");
                break;
            case '/':
                output.append("\\u002F");
                break;
            case '<':
                output.append("\\u003C");
                break;
            case '>':
                output.append("\\u003E");
                break;
            // Unicode
            case '\u2028':
                output.append("\\u2028");
                break;
            case '\u2029':
                output.append("\\u2029");
                break;

            default:
                output.append(c);
                break;
            }
        }
        return output.toString();
    }


    /**
     * JavaScript regex content escaper.
     * <p>
     * Escape for a JavaScript regular expression:
     * <pre>
     * &lt;script type="text/javascript"&gt;
     *   var b = /^JS_REGEX_ESCAPE_HERE/.test(document.location);
     * &lt;/script&gt;
     * </pre>
     * <p>
     * Note that when using a regular expression inside a JavaScript string such as:
     * <pre>&lt;script type="text/javascript"&gt;
     *   var b = (new RegExp('^CONTENT_HERE')).test(document.location);
     * &lt;/script&gt;</pre>
     * You should first escape using the {@link #jsRegex(String)} escaper, and make sure
     * that the JavaScript string itself is properly rendered using the {@link #jsString(String)}
     * escaper. This is a nested context scenario in which we have a JavaScript regex
     * inside a JavaScript string, for which we need to first escape the inner most context
     * and walking back the stack of context to the outer most one.
     * </p>
     * <p>
     * This JavaScript regex escaper processes the following characters:
     * <ul>
     * <li>
     * Regex characters: <code>\ (U+005C)</code>, <code>/ (U+002F)</code>, 
     *                   <code>( (U+0028)</code>, <code>[ (U+005B)</code>, 
     *                   <code>{ (U+007B)</code>, <code>] (U+005D)</code>, 
     *                   <code>} (U+007D)</code>, <code>) (U+0029)</code>, 
     *                   <code>* (U+002A)</code>, <code>+ (U+002B)</code>, 
     *                   <code>- (U+002D)</code>, <code>. (U+002E)</code>, 
     *                   <code>? (U+003F)</code>, <code>! (U+0021)</code>, 
     *                   <code>^ (U+005E)</code>, <code>$ (U+0024)</code>, 
     *                   <code>| (U+007C)</code> 
     * </li>
     * <li>
     * Control characters: <code>\t (U+0009)</code>, <code>\n (U+000A)</code>, 
     *                     <code>\v (U+000B)</code>, 
     *                     <code>\f (U+000C)</code>, <code>\r (U+000D)</code> 
     * </li>
     * </ul>
     *
     * @param  input the string to be escaped
     * @return       the escaped JavaScript regex or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String jsRegex(String input) {
        if (input == null)
            return null;

        int length = input.length();
        StringBuilder output = allocateStringBuilder(input, length);

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            switch (c) {
            // Control chars
            case '\t':
                output.append("\\t");
                break;
            case '\n':
                output.append("\\n");
                break;
            case '\u000b':
                output.append("\\v");
                break;
            case '\f':
                output.append("\\f");
                break;
            case '\r':
                output.append("\\r");
                break;
            // Escape sequence, and regexp terminator
            case '\\':
                output.append("\\\\");
                break;
            case '/':
                output.append("\\/");
                break;
            // Regexp specific characters
            case '(':
                output.append("\\(");
                break;
            case '[':
                output.append("\\[");
                break;
            case '{':
                output.append("\\{");
                break;
            case ']':
                output.append("\\]");
                break;
            case ')':
                output.append("\\)");
                break;
            case '}':
                output.append("\\}");
                break;
            case '*':
                output.append("\\*");
                break;
            case '+':
                output.append("\\+");
                break;
            case '-':
                output.append("\\-");
                break;
            case '.':
                output.append("\\.");
                break;
            case '?':
                output.append("\\?");
                break;
            case '!':
                output.append("\\!");
                break;
            case '^':
                output.append("\\^");
                break;
            case '$':
                output.append("\\$");
                break;
            case '|':
                output.append("\\|");
                break;

            default:
                output.append(c);
                break;
            }
        }
        return output.toString();
    }


    /**
     * CSS String escaper.
     * <p>
     * CSS escaper for strings such as CSS selector or quoted URI: 
     * <pre>
     * &lt;style"&gt;
     *  a[href *= "DATA_HERE"] {...}
     *  li { background: url('DATA_HERE'); }
     * &lt;/style&gt;
     * </pre>
     * <p>
     * This CSS string escaper processes the following characters:
     * <ul>
     * <li>
     * CSS string characters: <code>' (U+0022)</code>, <code>" (U+0027)</code>, 
     *                        <code>\ (U+005C)</code>
     * </li>
     * <li>
     * HTML characters: <code>/ (U+002F)</code>,
     *                  <code>&lt; (U+003C)</code>, <code>&gt; (U+003E)</code>, 
     *                  <code>&amp; (U+0026)</code>
     * </li>
     * <li>
     * Control characters: <code>\b (U+0008)</code>, 
     *                     <code>\t (U+0009)</code>, <code>\n (U+000A)</code>, 
     *                     <code>\f (U+000C)</code>, <code>\r (U+000D)</code> 
     * </li>
     * <li>
     * Unicode newlines: <code>LS (U+2028)</code>, <code>PS (U+2029)</code>
     * </li>
     * </ul>
     *
     * @param  input the string to be escaped
     * @return       the CSS string escaped or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String cssString(String input) {
        if (input == null)
            return null;

        int length = input.length();
        StringBuilder output = allocateStringBuilder(input, length);

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            switch (c) {
            // Control chars
            case '\b':
                output.append("\\08 ");
                break;
            case '\t':
                output.append("\\09 ");
                break;
            case '\n':
                output.append("\\0A ");
                break;
            case '\f':
                output.append("\\0C ");
                break;
            case '\r':
                output.append("\\0D ");
                break;
            // String chars
            case '\'':
                output.append("\\27 ");
                break;
            case '"':
                output.append("\\22 ");
                break;
            case '\\':
                output.append("\\5C ");
                break;
            // HTML chars for closing the parent context
            case '&':
                output.append("\\26 ");
                break;
            case '/':
                output.append("\\2F ");
                break;
            case '<':
                output.append("\\3C ");
                break;
            case '>':
                output.append("\\3E ");
                break;
            // Unicode
            case '\u2028':
                output.append("\\002028 ");
                break;
            case '\u2029':
                output.append("\\002029 ");
                break;

            default:
                output.append(c);
                break;
            }
        }
        return output.toString();
    }


    /**
     * SQL LIKE clause escaper.
     * <p>
     * This SQL LIKE clause escaper does not protect against SQL injection, but ensure
     * that the string to be consumed in SQL LIKE clause does not alter the current
     * LIKE query by inserting <code>%</code> or <code>_</code>: 
     * <pre>
     * entityManager.createQuery("FROM MyEntity e WHERE e.content LIKE :like_query ESCAPE '@'")
     *              .setParameter("like_query", "%" + Escape.sqlLikeClause(USER_DATA_HERE))
     *              .getResultList();
     * </pre>
     * This escaper has to be used with a safe SQL query construct such as the JPQL
     * named parameterized query in the previous example.
     * <p>
     * This escaper uses by default the <code>@</code> as escape character. The other method
     * {@link #sqlLikeClause(String,char)} allows for using a different escape character such as
     * <code>\</code>. 
     *
     * <p>
     * This SQL LIKE escaper processes the following characters:
     * <ul>
     * <li>
     * SQL LIKE characters: <code>_ (U+005F)</code>, <code>% (U+0025)</code>, 
     *                      <code>@ (U+0040)</code>
     * </li>
     * </ul>
     *
     * @param  input the string to be escaped
     * @return       the SQL LIKE escaped string or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String sqlLikeClause(String input) {
        return sqlLikeClause(input, '@');
    }


    /**
     * SQL LIKE clause escaper.
     * <p>
     * Similar to {@link #sqlLikeClause(String)}, but allows to specify the escape character
     * to be used. When a character different than <code>@</code> is used, <code>@</code> will
     * not be escaped by the escaper, and the specified escape character will be.
     *
     * @param  input  the string to be escaped
     * @param  escape the escape character to be used 
     * @return        the SQL LIKE escaped string or <code>null</code> if <code>input</code> is null
     * @since  1.0
     */
    public static String sqlLikeClause(String input, char escape) {
        if (input == null)
            return null;

        int length = input.length();
        StringBuilder output = allocateStringBuilder(input, length);

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (c == escape || c == '_' || c == '%') {
                output.append(escape); 
            }
            output.append(c); 
        }
        return output.toString();
    }


    /**
     * Compute the allocation size of the StringBuilder based on the input and its
     * length.
     */
    private static StringBuilder allocateStringBuilder(String input, int length) {
        // Allocate enough temporary buffer space to avoid reallocation in most
        // cases. If you believe you will output large amount of data at once
        // you might need to change the factor.
        int buflen = length;
        if (length * 2 > 0)
            buflen = length * 2;
        return new StringBuilder(buflen);
    }

}
