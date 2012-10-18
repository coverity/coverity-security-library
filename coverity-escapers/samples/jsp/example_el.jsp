<%--
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
 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://coverity.com/security" prefix="cov" %>
<!--
    Sample JSP page that contains some mocked-up CSS, JavaScript, 
    and HTML. Using EL for most of the data manipulation, and the 
    Coverity security escapers to make sure there is no cross-site
    scripting in the page.
    Note that this JSP is meant to be an example page on how to use the
    escapers developed by Coverity, not more.
-->
<!doctype html>
<html>
<head>
    <style>
    /*
        Look at the `currentPage` parameter, and highlight it in pink. We need
        to use a CSS string escaper since we put untrusted data in a CSS 
        selector (aka CSS string).
    */
    a [data-param *= '${cov:cssStringEscape(param.currentPage)}'] {
        background-color: pink;
        margin: 2px;
        padding: 1px 15px 1px 15px;
    }
    </style>
</head>
<body>
    <div id="container">
        <div id="first">
            <ul>
                <li class="active">
                    <!--
                        The `content` parameter needs to be added in the URL as a parameter,
                        so we can use URI encoding in this case.
                    -->
                    <a href="/path?content=${cov:uriEncode(param.content)}&amp;currentPage=firstLink"
                       data-param="firstLink">
                        First Link
                    </a>
                </li>
                <li>
                    <a data-param="secondLink" href="#" id="second-link">Second Link</a>
                </li>
            </ul>
        </div>
        <div id="second"></div>
        <div id="third">
        <%-- We assume we have an `entries` availalbe in our model or attributes --%>
        <c:forEach var="entry" items="${entries}">

            <div class="entry-container">
                <div class="entry-header">
                    <h1><a name="${fn:escapeXml(entry.title)}">${fn:escapeXml(entry.title)}</a></h1>
                </div>
                <div class="entry-body">
                    ${fn:escapeXml(entry.content)}
                </div>
                <div class="entry-footer">
                    <span class="one-liner">
                        <span class="pull-left">
                            Published: ${fn:escapeXml(entry.date)}
                        </span>
                        <span class="pull-right">
                            Author:
                            <!--
                                Inserting data in an HTML event attribute. This is eventually the following
                                nested contexts: HTML Double Quoted Attributed > JavaScript Single Quoted String
                                We need to make sure to escape properly for both context in the following order:
                                  1st- Escape JavaSript string
                                  2nd- Escape HTML attribute
                                The double escaping is required since the web browser will eventually do the following
                                actions:
                                  1st- Extract the content of the `onclick` attribute and process the HTML entities
                                  2nd- Pass the content to the JavaScript engine
                                  3rd- JavaScript engine will 
                            -->
                            <a onclick="pullAuthor('${fn:escapeXml(cov:jsStringEscape(entry.author))}');return false;"
                               href="#${fn:escapeXml(entry.title)}">
                               ${fn:escapeXml(entry.author)}
                            </a>
                        </span>
                    </span>
                </div>
            </div>
        </c:forEach>
        </div>
        <div id="fourth">
            <%-- 
                As we put the escapers in the classpath, we can also use the Java escapers
                within scriplets. First we get few parameters and add them to the page context.
            --%>
            <c:set var="param1" value="<%= request.getParameter("param1") %>" />
            <c:set var="param2" value="<%= request.getParameter("param2") %>" />
            <c:set var="cssImageURL" value="<%= request.getParameter("cssImageURL") %>" />


            <div id="<%= Escape.html(pageContext.getAttribute("param1")) %>">
                4th content
                <!--
                    Similarly to JavaScript in HTML events, style attributes need to be first
                    esaped for its own language contexts (in this example, CSS string), then
                    we need to escape for the outer context (HTML attribute).
                -->
                <span style="background-url: url('<%= Escape.html(Escape.cssString(pageContext.getAttribute("cssImageURL"))) %>')">
                    <c:out value="${param2}" />
                </span>
            </div>
        </div>
    </div>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
    <script type="text/javascript" src="/static/js/other-libs.min.js"></script>
    <script type="text/javascript">
    // Insert untrusted parameter `anchor` into a JavaScript string; 
    // need to use JavaScript string escaping. This escaper will ensure
    // that the JavaScript is not escaped out, and also that the literal
    // closing script tag does not appear in the string as well (in addition
    // to new line characters, etc.)
    window.AnchorContent = '${cov:jsStringEscape(param.anchor)}';

    $(document).ready(function(){
        $(document).on('click', '#second-link', function() {
            // Add the untrusted data safely to our page using jQuery
            $("#second").append(
                $("<div>")
                .attr('style', 'background-color:#333')
                .text(window.AnchorContent)
            );
        });
    });
    </script>
</body>
</html>
