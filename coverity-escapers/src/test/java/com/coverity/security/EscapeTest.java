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
package com.coverity.testsuite;

import com.coverity.security.Escape;
import com.coverity.security.EscapeEL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Unit tests for imporant characters
public class EscapeTest extends TestCase {

    public final static String[] WEB_NEW_LINES = {
        "\n", "\r", "\f",
        "\u2028", "\u2029"
    };

    public final static String[] WEB_WHITESPACES = {
        " ", "\t"
    };

    public final static String[] HTML_SENSITIVE_CHARS = {
        "<", ">",  // HTML tags
        "'", "\"", // HTML attributes
        " ", "/"   // HTML tag/attribute name
    };

    public final static String[] JS_STRING_SENSITIVE_CHARS = {
        "'", "\"",     // JavaScript string transition
        "<", "/"       // Potential HTML </script> transition
    };

    public final static String[] CSS_STRING_SENSITIVE_CHARS = {
        "'", "\"",     // CSS string transition
        "<", ">", "&"  // Potential HTML </style> transition
    };

    public EscapeTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(EscapeTest.class);
    }

    public void testHTMLEscaper_Transtions() {
        // Simple check for full escaped charcters
        for (int i=0; i < HTML_SENSITIVE_CHARS.length; i++) {
            String chr = HTML_SENSITIVE_CHARS[i];
            assertTrue(!Escape.html(chr).contains(chr));
        }
    }

    public void testCSSStringEscaper_Transtions() {
        for (int i=0; i < CSS_STRING_SENSITIVE_CHARS.length; i++) {
            String chr = CSS_STRING_SENSITIVE_CHARS[i];
            assertTrue(!Escape.cssString(chr).contains(chr));
        }
    }

    public void testJSStringEscaper_Transtions() {
        for (int i=0; i < JS_STRING_SENSITIVE_CHARS.length; i++) {
            String chr = JS_STRING_SENSITIVE_CHARS[i];
            assertTrue(!Escape.jsString(chr).contains(chr));
        }
    }

    public void testAllStringEscaper_Newlines() {
        for (int i=0; i < WEB_NEW_LINES.length; i++) {
            String chr = WEB_NEW_LINES[i];
            assertTrue(!Escape.html(chr).contains(chr));
            assertTrue(!Escape.cssString(chr).contains(chr));
            assertTrue(!Escape.jsString(chr).contains(chr));
        }
    }

    public void testHTMLEscaper_Whitespace() {
        for (int i=0; i < WEB_WHITESPACES.length; i++) {
            String chr = WEB_WHITESPACES[i];
            assertTrue(!Escape.html(chr).contains(chr));
        }
    }

    public void testHTMLEscaper_String() {
        // Assume the string is within any HTML tag, like <div>:
        // <div>TAINTED_DATA_HERE</div>
        // or the content of an HTML attibute (not DOM event or CSS style)
        // <div data-param="TAINTED_DATA_HERE">...
        String beforeEscape = "</div><script src=\"http://example.com/?evil=true&param=xss\">"
                            + "\\ Foobar & '\"><img src=. onerorr=alert(1) > ";
        String afterEscape = Escape.html(beforeEscape)
                           + EscapeEL.htmlEscape(beforeEscape);

        String[] badSequences = {
            "<", ">", "<script", "</div", 
            "\\", "'", " ", "& "
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence));         
        }
    }

    public void testHTMLTextEscaper_String() {
        // This escaper Escape.htmlText is a relaxed version of the Escape.html
        // it only escapes ' " < > & and is sufficient when ALWAYS using quoted
        // attributes.
        //
        // Assume the string is within any HTML tag, like <div>:
        // <div>TAINTED_DATA_HERE</div>
        // or the content of an HTML attibute (not DOM event or CSS style)
        // <div data-param="TAINTED_DATA_HERE">...
        String beforeEscape = "</div><script src=\"http://example.com/?evil=true&param=xss\">"
                            + "Foobar & '\"><img src=. onerorr=alert(1) > ";
        String afterEscape = Escape.htmlText(beforeEscape)
                           + EscapeEL.htmlText(beforeEscape);

        String[] badSequences = {
            "<", ">", "<script", "</div", 
            "'", "\"", "& "
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence));         
        }
    }

    public void testURIEncoder() {
        // Assume the string is within an HTML <script> tag, like so:
        // <a href="foobar?value=TAINTED_DATA_HERE">
        String beforeEscape = "close context'\" break context "
                            + "& + : % </script>"
                            + "\t \n \f \r (!#foobar$) *.*=?[@]";
        String afterEscape = Escape.uri(beforeEscape) 
                           + EscapeEL.uriEncode(beforeEscape)
                           + EscapeEL.uriParamEncode(beforeEscape);

        String[] badSequences = {
            "% ",
            "'", "\"",
            "+", "\t", "\n", "\f", "\r",
            "(", "!", "#", "$", ")", "*", ".", "=", "?",
            "[", "@", "]"
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence));         
        }
    }

    public void testJSStringEscaper_String() {
        // Assume the string is within an HTML <script> tag, like so:
        // <script> var = 'TAINTED_DATA_HERE'; </script>
        String beforeEscape = "close context'\" continue context \\ break context "
                            + "\u2029 \u2028 escape HTML context & </script>"
                            + " control chars: \b \t \n \u000b \f";
        String afterEscape = Escape.jsString(beforeEscape)
                           + EscapeEL.jsStringEscape(beforeEscape);

        String[] badSequences = {
            "'",
            "\"",
            " \\ ",
            "\u2028",
            "\u2029",
            "&", "\b", "\t", "\n", "\u000b", "\f",
            "</script>",
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence));         
        }
    }


    public void testJSRegexEscaper_String() {
        // Assume the string is within a JavaScript regex:
        // <script> var b = /^TAINTED_DATA_HERE/.test("foo"); </script>
        String beforeEscape = "close context / continue context \\ break context "
                            + "\u2029 \u2028 escape HTML context & </script>"
                            + " ( ) [ ] { } * + - . ? ! ^ $ | "
                            + " control chars: \t \n \u000b \f \r ";
        String afterEscape = Escape.jsRegex(beforeEscape)
                           + EscapeEL.jsRegexEscape(beforeEscape);

        String[] badSequences = {
            "\t", "\n", "\u000b", "\f", "\r",
            "</script>", " \\ ", " / ",
            " ( ", " ) ", " [ ", " ] ", " { ", " } ", " * ",
            " . ", " + ", " - ", " ? ", " ! ", " ^ ", " $ ",
            " | "
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence)); 
        }
    }

    public void testCSSStringEscaper_String() {
        // Assume the string is within an HTML <style> tag, like so:
        // <style> li [id *= 'TAINTED_DATA_HERE'] { ... } </style>
        String beforeEscape = "close context' \" continue context \\ break context \n"
                            + " escape HTML context </style>"
                            + " control chars: \b \t \n \f \r";
        String afterEscape = Escape.cssString(beforeEscape)
                           + EscapeEL.cssStringEscape(beforeEscape);

        String[] badSequences = {
            "'",
            "\\ ",
            "\n", "\r", "\t", "\f", "\r",
            "\"",
            "</style>",
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence));         
        }
    }

    public void testNestedURIInHTMLEscaper_String() {
        // Assume the string is within an HTML <a> tag, like so:
        //   <a href="TAINTED_DATA_HERE">
        String beforeEscape = "javascript:alert(1); escape parent context \" "
                            + " break context % escape HTML context </a>"
                            + " data:text/html,<script>alert(1)</script>";
        String afterEscape = Escape.html(Escape.uri(beforeEscape));
        String[] badSequences = {
            "javascript:",
            "data:",
            "(1);",
            "\"",
            " % ",
            "</a>"
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence));         
        }     
    }

    public void testNestedURLInCSSInHTMLEscaper_String() {
        // Assume the string is within an HTML style attribute, like so:
        // <span style="background-image:url('TAINTED_DATA_HERE')">...
        String beforeEscape = "javascript:alert(1) break child context % close parent context ') escape"
                            + " parent context \" escape parent context </span>";
        String afterEscape = Escape.html(Escape.cssString(Escape.uri(beforeEscape)));
        String[] badSequences = {
            "javascript:",
            "javascript&#3A;", // shouldn't occur, in case it did would still fire javascript: uri
            " % ",
            " &#25; ",
            "')",
            "\n",
            "\"",
            "</span>"
        };

        for (int i=0; i < badSequences.length; i++) {
            String badSequence = badSequences[i];
            assertTrue(!afterEscape.contains(badSequence));         
        }     

    }

    public void testForNullInput() {
        // The test for null inputs is useful to make sure that we do not throw an 
        // exception when receiving an null EL variable (quite common scenario) 
        try {
            Escape.html(null);
            Escape.htmlText(null);
            Escape.jsString(null);
            Escape.jsRegex(null);
            Escape.cssString(null);
            Escape.uri(null);
            Escape.uriParam(null);
            Escape.sqlLikeClause(null, '\\');
            Escape.sqlLikeClause(null);
        }
        catch(Exception ex) {
            // Test must fail if any exception is thrown
            assertTrue(false);
        }
    }

    public void testSQLLikeEscaper_String() {
        assertTrue(Escape.sqlLikeClause("%_@'+=").equals("@%@_@@'+="));
        assertTrue(Escape.sqlLikeClause("%_@'+=\\", '\\').equals("\\%\\_@'+=\\\\"));
    }

}