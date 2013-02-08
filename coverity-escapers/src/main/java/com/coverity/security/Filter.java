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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Filter {

    private static final Pattern NUMBER_REGEX = Pattern.compile("[-+]?(\\d*\\.)?\\d+\\.?");

    public static String asNumber(String number) {
        if (number == null)
            return null;
        if (NUMBER_REGEX.matcher(number).matches())
            return number;
        return null;
    }


    private static final Pattern HEX_REGEX = Pattern.compile("[\\da-fA-F]+");

    public static String asHex(String hex) {
        if (hex == null)
            return null;
        if (HEX_REGEX.matcher(hex).matches())
            return hex;
        return null;
    }


    private static final Pattern CSS_HEX_COLOR_REGEX = Pattern.compile("#[\\da-fA-F]{3}([\\da-fA-F]{3})?");

    public static String asCssHexColor(String hexColor) {
        if (hexColor == null)
            return null;
        if (CSS_HEX_COLOR_REGEX.matcher(hexColor).matches())
            return hexColor;
        return null;
    }


    private static final Pattern CSS_NAMED_COLOR_REGEX = Pattern.compile("[\\w]+");

    public static String asCssNamedColor(String namedColor) {
        if (namedColor == null)
            return null;
        if (CSS_NAMED_COLOR_REGEX.matcher(namedColor).matches())
            return namedColor;
        return null;
    }


}
