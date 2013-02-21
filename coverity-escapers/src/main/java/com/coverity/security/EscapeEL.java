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

import com.coverity.security.Escape;

/**
 * EscapeEL is a wrapper class the provides alternative names for the escaping
 * methods in com.coverity.security.Escape. These alternative names are useful
 * primarily as EL functions in JSP files.
 * <p>
 * To use these functions in EL, use mvn package and then drop
 * <code>coverity-escapers-X.X.jar</code> into <code>WEB-INF/lib</code>. Then you can use the
 * following incantation to incorporate the tag library into EL to invoke these
 * functions:
 * <pre>
 * &lt;%@ taglib uri="http://coverity.com/security" prefix="cov" %&gt;
 * 
 * &lt;!-- Example of usage within a JSP --&gt; 
 * &lt;script type="text/javascript"&gt;
 *     var x = '${cov:jsStringEscape(param.foobar)}';
 * &lt;/script&gt;
 * </pre>
 * @author Romain Gaucher
 * @author Andy Chou
 * @author Jon Passki
 * 
 */
public class EscapeEL {

    /**
     * EL wrapper for {@link Escape#html(String)}
     */
    public static String htmlEscape(String input) {
        return Escape.html(input);
    }

    /**
     * EL wrapper for {@link Escape#htmlText(String)}, equivalent to <code>fn:escapeXml</code>.
     */
    public static String htmlText(String input) {
        return Escape.htmlText(input);
    }

    /**
     * EL wrapper for {@link Escape#uriParam(String)}
     */
    public static String uriParamEncode(String input) {
        return Escape.uriParam(input);
    }

    /**
     * EL wrapper for {@link Escape#uri(String)}
     */
    public static String uriEncode(String input) {
        return Escape.uri(input);
    }

    /**
     * EL wrapper for {@link Escape#jsString(String)}
     */
    public static String jsStringEscape(String input) {
        return Escape.jsString(input);
    }

    /**
     * EL wrapper for {@link Escape#jsRegex(String)}
     */
    public static String jsRegexEscape(String input) {
        return Escape.jsRegex(input);
    }

    /**
     * EL wrapper for {@link Escape#cssString(String)}
     */
    public static String cssStringEscape(String input) {
        return Escape.cssString(input);
    }
}
