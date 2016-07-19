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

import com.coverity.security.Filter;
import com.coverity.security.FilterEL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.System;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;

// Unit tests for imporant characters
public class FilterTest extends TestCase {

    public FilterTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(FilterTest.class);
    }

    private static String CssDefault = "invalid";
    private static String testAsColorWrapper(String color) {
        String filtered = Filter.asCssColor(color);

        if (filtered == null || (filtered.equals(CssDefault) && !color.trim().equals(CssDefault))) {
            return null;
        } else {
            return color;
        }
    }

    private static String testAsColorWrapperEL(String color) {
        String filtered = FilterEL.asCssColor(color);

        if (filtered == null || (filtered.equals(CssDefault) && !color.trim().equals(CssDefault))) {
            return null;
        } else {
            return color;
        }
    }

    private static String CssDefault2 = "blue";
    private static String testAsColorWrapperDefault(String color) {
        String filtered = Filter.asCssColor(color, CssDefault2);

        if (filtered == null || (filtered.equals(CssDefault2) && !color.trim().equals(CssDefault2))) {
            return null;
        } else {
            return color;
        }
    }

    private static String testAsColorWrapperELDefault(String color) {
        String filtered = FilterEL.asCssColorDefault(color, CssDefault2);

        if (filtered == null || (filtered.equals(CssDefault2) && !color.trim().equals(CssDefault2))) {
            return null;
        } else {
            return color;
        }
    }

    public void testAsCssColor() {
        String[] colorFalseTests = {
            //named Color
            "#1",
            "this is not a name",
            "efe fef",
            "foo()<>{}",
            "\09 thisIsPossibleButNotConsidered",

            //Hex Color
            "#1",
            "12345",
            "#12",
            "#1223",
            "#12233",
            "#122g34",
            "\0#123",
            "\f#123",
            "\n#123",
            ""
        };

        String[] colorTrueTests = {
            //Named Color
            "AliceBlue",
            "white",
            "PaleVioletRed",

            //Hex Color
            "#fff",
            "#FFF",
            "#0fF056"
        };

        runTrueFalseCases(colorFalseTests,
                          colorTrueTests,
                          FilterTest.class,
                          "testAsColorWrapper",
                          /*printIter*/false);

        runTrueFalseCases(colorFalseTests,
                          colorTrueTests,
                          FilterTest.class,
                          "testAsColorWrapperEL",
                          /*printIter*/false);

        runTrueFalseCases(colorFalseTests,
                          colorTrueTests,
                          FilterTest.class,
                          "testAsColorWrapperDefault",
                          /*printIter*/false);

        runTrueFalseCases(colorFalseTests,
                          colorTrueTests,
                          FilterTest.class,
                          "testAsColorWrapperELDefault",
                          /*printIter*/false);
    }

    private static String testAsNumberWrapper(String number) {
        String filtered = Filter.asNumber(number);

        if (filtered == null || (filtered.equals("0") && !(number.trim().equals("0")))) {
            return null;
        } else {
            return number;
        }
    }

    private static String testAsNumberWrapperEL(String number) {
        String filtered = FilterEL.asNumber(number);

        if (filtered == null || (filtered.equals("0") && !(number.trim().equals("0")))) {
            return null;
        } else {
            return number;
        }
    }

    private static String defaultNumber = "1";
    private static String testAsNumberWrapperDefault(String number) {
        String filtered = Filter.asNumber(number, defaultNumber);

        if (filtered == null || (filtered.equals(defaultNumber) && !(number.trim().equals(defaultNumber)))) {
            return null;
        } else {
            return number;
        }
    }

    private static String testAsNumberWrapperELDefault(String number) {
        String filtered = FilterEL.asNumberDefault(number, defaultNumber);

        if (filtered == null || (filtered.equals(defaultNumber) && !(number.trim().equals(defaultNumber)))) {
            return null;
        } else {
            return number;
        }
    }

    public void testAsNumber() {
        String[] numberFalseTests = {
            //asNumber
            ".",
            "+65266+",
            "-+1.266",
            "65.65.",

            //asHex
            "0xefefefg",
            "0xag",
            "abc",
            "\\x15"
        };
        String[] numberTrueTests = {
            //asNumber
            "+1.425",
            "65.",
            "-64.32",
            "42",
            "-.04",
            "0.2323232",

            //asHex
            "0xefefef",
            "0x0ff",
            "0x234345"
        };

        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          FilterTest.class,
                          "testAsNumberWrapper",
                          /*printIter*/false);

        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          FilterTest.class,
                          "testAsNumberWrapperEL",
                          /*printIter*/false);

        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          FilterTest.class,
                          "testAsNumberWrapperDefault",
                          /*printIter*/false);

        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          FilterTest.class,
                          "testAsNumberWrapperELDefault",
                          /*printIter*/false);
    }

    private static String testAsNumberWrapperOctal(String number) {
        String filtered = Filter.asNumber(number);

        if ((filtered == null)||(filtered.trim().charAt(0) == '0' && filtered.length() > 1)) {
            return null;
        } else if (Integer.toString(Integer.parseInt(number)).equals(filtered)) {
            return number;
        } else {
            return null;
        }
    }

    private static String testAsNumberWrapperOctalEL(String number) {
        String filtered = FilterEL.asNumber(number);

        if ((filtered == null)||(filtered.trim().charAt(0) == '0' && filtered.length() > 1)) {
            return null;
        } else if (Integer.toString(Integer.parseInt(number)).equals(filtered)) {
            return number;
        } else {
            return null;
        }

    }

    public void testAsNumberOctal() {
        String[] numberFalseTests = {
        };
        String[] numberTrueTests = {
            "0777"
        };

        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          FilterTest.class,
                          "testAsNumberWrapperOctal",
                          /*printIter*/false);

        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          FilterTest.class,
                          "testAsNumberWrapperOctalEL",
                          /*printIter*/false);
    }

    //A dodgy wrapper that we're going to use so that we can use the existing infrastructure
    private static String testFlexibleURLWrapper(String url) {
        String filtered = Filter.asFlexibleURL(url);

        if (filtered == null || (filtered.startsWith("./") && !url.startsWith("./"))) {
            return null;
        } else {
            return url;
        }
    }

    public void testFlexibleURLSchemeLookup() {
        String[] correctSchemeSequences = {
          "johndoe://baz",
          "john.doe://baz",
          "john+doe://baz",
          "john-doe://baz",
          "JOHN-doe://baz",
          "j0hn-d0e://baz"
        };

        String[] incorrectSchemeSequences = {
          "john\ndoe://baz",
          "john\rdoe://baz",
          "jo\0\0\0hn&*doe://baz",
          "JOHN(.)doe://baz",
          "j0hn#-$d0e://baz",
          "JOHN(.)doe://baz",
          "\u3456j0hn#-$d0e",
          "some-string\n"
        };

        for (int i=0; i < correctSchemeSequences.length; i++) {
            String correctScheme = correctSchemeSequences[i];
            String afterFilter = Filter.asFlexibleURL(correctScheme);
            assertTrue(afterFilter.equals(correctScheme));
        }

        for (int i=0; i < incorrectSchemeSequences.length; i++) {
            String incorrectScheme = incorrectSchemeSequences[i];
            String afterFilter = Filter.asFlexibleURL(incorrectScheme);

            assertTrue(!afterFilter.equals(incorrectScheme));
        }
    }

    private static String testFlexibleURLWrapperEL(String url) {
        String filtered = FilterEL.asFlexibleURL(url);

        if (filtered == null || (filtered.startsWith("./") && !url.startsWith("./"))) {
            return null;
        } else {
            return url;
        }
    }

    private static String testURLWrapper(String url) {
        String filtered = Filter.asURL(url);

        if (filtered == null || (filtered.startsWith("./") && !url.startsWith("./"))) {
            return null;
        } else {
            return url;
        }
    }

    private static String testURLWrapperEL(String url) {
        String filtered = FilterEL.asURL(url);

        if (filtered == null || (filtered.startsWith("./") && !url.startsWith("./"))) {
            return null;
        } else {
            return url;
        }
    }

    public void testURL() {
        final String[] urlFalseTests = {
            "javascript:test('http:')",
            "jaVascRipt:test",
            "\\UNC-PATH\\",
            "data:test",
            "about:blank",
            "javascript\n:",
            "vbscript:IE",
            "data&#58boo",
            "dat\0a:boo",
            "h*t90.://foo",
            "h-aa/a://aaa",
            "ABOUT:test/something"
        };
        final String[] urlTrueTests = {
            "\\\\UNC-PATH\\",
            "http://host/url",
            "hTTp://host/url",
            "//coverity.com/lo",
            "/base/path",
            "https://coverity.com",
            "mailto:srl@coverity.com",
            "maiLto:srl@coverity.com",
            "ftp://coverity.com/elite.warez.tgz",
            ""
        };

        runTrueFalseCases(urlFalseTests,
                          urlTrueTests,
                          FilterTest.class,
                          "testURLWrapper",
                          /*printIter*/false);

        runTrueFalseCases(urlFalseTests,
                          urlTrueTests,
                          FilterTest.class,
                          "testURLWrapperEL",
                          /*printIter*/false);

        //Test the blacklist implementation

        final String[] urlFlexibleFalseTests = {
            };

        final String[] urlFlexibleTrueTests = {
                "tel:5556667777",
                "gopher:something something",
                "test.html"
            };

        runTrueFalseCases(urlFlexibleFalseTests,
                urlFlexibleTrueTests,
                FilterTest.class,
                "testFlexibleURLWrapper",
                /*printIter*/false);

        runTrueFalseCases(urlFlexibleFalseTests,
                urlFlexibleTrueTests,
                FilterTest.class,
                "testFlexibleURLWrapperEL",
                /*printIter*/false);

        runTrueFalseCases(urlFalseTests,
                urlTrueTests,
                FilterTest.class,
                "testFlexibleURLWrapper",
                /*printIter*/false);

        runTrueFalseCases(urlFalseTests,
                urlTrueTests,
                FilterTest.class,
                "testFlexibleURLWrapperEL",
                /*printIter*/false);
    }

    private void runTrueFalseCases(String[] falseCases,
            String[] trueCases,
            String testedFunction,
            boolean printIter) {
        runTrueFalseCases(falseCases, trueCases, Filter.class, testedFunction, printIter);
        runTrueFalseCases(falseCases, trueCases, FilterEL.class, testedFunction, printIter);
    }

    private void runTrueFalseCases(String[] falseCases,
                                   String[] trueCases,
                                   Class testedClass,
                                   String testedFunction,
                                   boolean printIter) {
        try {
            Method m = testedClass.getDeclaredMethod(testedFunction,
                                                      new Class[] { String.class });

            Object nullObject = null;

            for (int i=0; i<falseCases.length; i++) {
                String testcase = falseCases[i];
                if (printIter)
                    System.out.println(testcase + " -> " + m.invoke(nullObject, new Object[] {testcase}));
                assertTrue(m.invoke(nullObject, new Object[] {testcase}) == null);
            }

            for (int i=0; i<trueCases.length; i++) {
                String testcase = trueCases[i];
                if (printIter)
                    System.out.println(testcase + " -> " + m.invoke(nullObject, new Object[] {testcase}));
                String result = (String)m.invoke(nullObject, new Object[] {testcase});
                assertTrue(result != null);
                assertTrue(result.equals(testcase));
            }

            // Assert null
            String nullString = null;
            assertTrue(m.invoke(nullObject, new Object[] {nullString}) == null);
        }
        catch (NoSuchMethodException ex) {
            System.out.println(ex.getMessage());
            assertTrue(false);
        }
        catch (IllegalAccessException ex) {
            System.out.println(ex.getMessage());
            assertTrue(false);
        }
        catch (InvocationTargetException ex) {
            System.out.println(ex.getMessage());
            assertTrue(false);
        }
    }


}
