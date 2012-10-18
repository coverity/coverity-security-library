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
package not.a.package;

import java.lang.StringBuilder;
// Import the Coverity escapers
import com.coverity.security.Escape;

// Possible location of our ExampleBean
import does.not.exist.ExampleBean;

public class ExampleJavaHtmlEscapers {

    @Autowired
    public ExampleBean exampleBean;

    // An example of using the escapers in Java.
    // This object class supposedly get a bean, and
    // generate an HTML blob to represent it.
    public String toHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='example'>\n");

        // Add the title
        sb.append("  <span class='example-title'>\n");
        sb.append(Escape.html(exampleBean.getTitle()));
        sb.append("\n  </span>\n");

        // Write a JavaScript string
        sb.append("  <script type='text/javascript'>\n");
        sb.append("  window.FooBar = '");
        sb.append(Escape.jsString(exampleBean.getContent()));
        sb.append("';\n");
        sb.append("  </script>\n");

        // Other available methods are:
        // - Escape.cssString(String)String
        // - Escape.uri(String)String
        // - Escape.sqlLikeClause(String)String

        return sb.toString();
    }

}