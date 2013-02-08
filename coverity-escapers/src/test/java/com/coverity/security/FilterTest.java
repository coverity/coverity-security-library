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


    public void testAsCssNamedColor() {
        String[] numberFalseTests = {
            "#1",
            "this is not a name",
            "efe fef",
            "foo()<>{}",
            "\09 thisIsPossibleButNotConsidered",
            "#efefef"
        };

        String[] numberTrueTests = {
            "AliceBlue",
            "white",
            "PaleVioletRed"
        };

        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          "asCssNamedColor",
                          /*printIter*/false);
    }

    public void testAsCssHexColor() {
        String[] numberFalseTests = {
            "#1",
            "12345",
            "efefef",
            "#12",
            "#1223",
            "#12233",
            "#122g34",
            "\0#123",
            "\f#123",
            "\n#123",
            ""
        };

        String[] numberTrueTests = {
            "#fff",
            "#FFF",
            "#0fF056"
        };
        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          "asCssHexColor",
                          /*printIter*/false);
    }

    public void testAsHex() {
        String[] numberFalseTests = {
            "efefefg",
            "0.23233112",
            "0xdfdfdf"
        };

        String[] numberTrueTests = {
            "efefef",
            "0ff",
            "234345"
        };
        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          "asHex",
                          /*printIter*/false);
    }

    public void testAsNumber() {
        String[] numberFalseTests = {
            ".",
            "+65266+",
            "-+1.266"
        };
        String[] numberTrueTests = {
            "+1.425",
            "-.04",
            "65.",
            "-64.32",
            "42"
        };
        runTrueFalseCases(numberFalseTests,
                          numberTrueTests,
                          "asNumber",
                          /*printIter*/false);
    }


    private void runTrueFalseCases(String[] falseCases,
                                   String[] trueCases,
                                   String testedFunction,
                                   boolean printIter) {
        try {
            Method m = Filter.class.getDeclaredMethod(testedFunction, 
                                                      new Class[] { String.class });
            Method n = FilterEL.class.getDeclaredMethod(testedFunction, 
                                                      new Class[] { String.class });

            Object nullObject = null;

            for (int i=0; i<falseCases.length; i++) {
                String testcase = falseCases[i];
                if (printIter)
                    System.out.println(testcase + " -> " + m.invoke(nullObject, new Object[] {testcase}));
                assertTrue(m.invoke(nullObject, new Object[] {testcase}) == null);
                assertTrue(n.invoke(nullObject, new Object[] {testcase}) == null);
            }

            for (int i=0; i<trueCases.length; i++) {
                String testcase = trueCases[i];
                if (printIter)
                    System.out.println(testcase + " -> " + m.invoke(nullObject, new Object[] {testcase}));
                assertTrue(m.invoke(nullObject, new Object[] {testcase}) == testcase);
                assertTrue(n.invoke(nullObject, new Object[] {testcase}) == testcase);
            }

            // Assert null
            String nullString = null;
            assertTrue(m.invoke(nullObject, new Object[] {nullString}) == null);
            assertTrue(n.invoke(nullObject, new Object[] {nullString}) == null);
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
