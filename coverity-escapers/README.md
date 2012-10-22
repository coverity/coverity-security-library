
# Escape
 
Escape is a small set of methods for escaping tainted data. These escaping
methods are useful in transforming user-controlled ("tainted") data into
forms that are safe from being interpreted as something other than data, such
as HTML with JavaScript (typical cross-site scripting attack).

At this time these escaping routines mostly focus on cross-site scripting (XSS)
mitigations. Each method is good for different HTML contexts. (See below for a 
discussion on different contexts.)

While Coverity's static analysis product references these escaping routines
and understands their behavior; however, there is no dependency on
Coverity products. This library is completely standalone. Feel free to
use them! Just make sure you use them correctly :)

### Table of Contents
1. [Installation](#main_install)
2. [Usage](#main_usage)
3. [HTML Contexts Examples](#main_contexts)
4. [Authors & License](#main_authors)

# <a id="install"></a>Installation

## Using Maven
To include this library into your Maven project, add the following to your pom:

```xml
<dependency>
    <groupId>com.coverity.security</groupId>
    <artifactId>coverity-escapers</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Manually Build and Deploy
We use maven to build the library, and you can simply do:

    $ cd coverity-security-library
    $ mvn package

A JAR file will be created in the `coverity-escapers/target` directory. You can take
this JAR file `coverity-escaper-1.0.0.jar` and place it in the `WEB-INF/lib` of your
application.

## Build the Javadoc
The javadoc can be created directly from the Maven build:

    $ cd coverity-security-library
    $ mvn install
    $ open ./coverity-escapers/target/apidocs/index.html

# <a id="main_usage"></a> Usage

## Example 1: XSS Defect in Java Servlet

### Before Remediation

The servlet below takes a request parameter called `index` and directly inserts
it into the output within an HTML context, creating an XSS defect.

```java
public class IndexServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                         throws ServletException, IOException {
        String param = request.getParameter("index");           
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.write("<html><body>Index requested: " + param);
```

### After Remediation

To remedy, the Escape library needs to be imported into the project and then the
`Escape.html` method should wrap the `param` at the injection point.

```java
import com.coverity.security.Escape;
// ...
public class IndexServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                         throws ServletException, IOException {
        String param = request.getParameter("index");           
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.write("<html><body>Index requested: " + Escape.html(param));
```

## Example 2: XSS Defect in JSP EL

### Before Remediation

The JSP below takes a request parameter called `param.needHelp` and inserts
it into the page within a JavaScript single-quoted string context, within a
parent HTML double-quoted attribute context, creating an XSS defect. It uses
Expression Language (EL) to insert the value. While this tainted data is wrapped
by the JSTL `fn:escapeXml` method, the defect still exists because the underlying
JavaScript string context is not addressed.

```jsp
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!doctype html>
<html>
<head>
    <script src="/static/js/main.js"></script>
</head>
<body>
<span onmouseover="lookupHelp('${fn:escapeXml(param.needHelp)}');">
    Hello Blogger!
</span>
```

### After Remediation

To remedy this defect, the Escape library needs to be imported into the project
and then the `cov:jsStringEscape` EL method needs to wrap the `param.needHelp` at
the injection point. The outer `fn:escapeXml` method should still be used to
ensure values are properly escaped for the HTML attribute value context.

```jsp
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="cov" uri="http://coverity.com/security" %>

<!doctype html>
<html>
<head>
    <script src="/static/js/main.js"></script>
</head>
<body>
<span onmouseover="lookupHelp('${fn:escapeXml(cov:jsStringEscape(param.needHelp))}');">
    Hello Blogger!
</span>
```

Note that if you want to limit the number of EL functions imported, you can use the 
`cov:htmlEscape` function instead of `fn:escapeXml`.

# <a id="main_contexts"></a> Background Information

## Contexts

When fixing a defect, you need to understand the current context, the safety
obligations for that context, and what characters or sequences violate these
obligations. A context defines a subset of a language and syntax rules. For
example, the following `TAINTED_DATA_HERE` text occurs in an HTML double-quoted
attribute context.

```html
<span id="TAINTED_DATA_HERE">Some text here</span>
```

When tainted data is able to circumvent a context, it can lead to a security
defect, such as a cross-site scripting (XSS), SQL injection (SQLi), etc.. For
example, once outside of an HTML double-quoted attribute context, the inserted
data can create a new attribute such as `onmouseover`. This attribute name is a
DOM event handler. Browsers interpret the `onmouseover` attribute value as 
JavaScript, permitting an XSS defect.

Each context has a set of safety obligations many of which are met by not
inserting characters with special meaning within that context. The purpose of
this library is to assist developers by sanitizing tainted data for some common
contexts. However, not all contexts are addressed by this library. Some
contexts require more than character-level safety obligations and therefore
it is not possible to create escapers for these. For example, when inserting 
characters into an HTML attribute name, not only are certain characters disallowed, 
but a set of names should also be disallowed since they might create an XSS defect.

## Nested Contexts

A nested context occurs when more than one context exists for a given piece of
data. An example is the common HTML `<a>` anchor element and its `onclick` attribute:

```html
<a onclick="pullAuthor('TAINTED_DATA_HERE');return false;">...
```

In the example, there are currently two contexts that have safety obligations
for `TAINTED_DATA_HERE`:

* HTML double-quoted attribute
* JavaScript single-quoted string

Common libraries exist for sanitizing user data for the first context (HTML escaping is fine). 
However, if the JavaScript string context is left untreated, an attacker can execute an 
XSS attack in it. More so, HTML entity encoding the single quote `'` to `&#39;`
is ineffective. In this context, the browser decodes the HTML entity back to a
single quote when passing it to the JavaScript engine. Therefore, to fully 
remedy this defect, the safety obligations of the JavaScript string context need
to be met before the obligations of the HTML context. 

Remember that with nested contexts, order of escaping matters.

## Common Contexts

### HTML

The Escape library groups the following HTML contexts as one:

* HTML [normal element] [5] / PCDATA
* HTML [single and double-quoted attributes] [6]

HTML normal element injection example:

```html
<span>TAINTED_DATA_HERE</span>
```

HTML quoted attribute injection example:

```html
<div id="TAINTED_DATA_HERE">
    <span id='TAINTED_DATA_HERE_TOO'>Testing blog</span>
</div>
```

The Escape library meets the security obligations of these contexts by encoding
sensitive characters as HTML character references.

### JavaScript Strings (Single and Double Quoted)

ECMA 262 defines the [ECMAScript language] [2], of which JavaScript is a dialect.
The standard defines a string literal syntax for both ' and " strings in section
7.8.4 (of the ECMA PDF file).

Injection example:

```js
var blogComment = 'TAINTED_DATA_HERE';
logBlogComment(blogComment, "TAINTED_DATA_HERE_TOO");
```

The Escape library meets the security obligations of these contexts by escaping
these characters using JavaScript Unicode escaping. In addition, since JavaScript
is usually embedded withing a script tag, the JavaScript string context inherits
the security obligations which apply for the script tag. This is easily
summarized as the tag should not be closed, and the string literal `</script>`
should not appear in the JavaScript string. For this purpose, we also escape
the `/` character.

### CSS Strings (Single and Double Quoted)

CSS Level 2, Revision 1 (CSS 2.1) defines single-quoted (', U+0027) and
double-quoted (", U+0022) [strings] [3]. These strings are also used within a URL
quoted context and have the same obligations within that context.

Injection example:
```css
span[id="TAINTED_DATA_HERE"] {
  background-color: #efefef;
}
```
The Escape library meets the security obligations of these contexts by escaping
these characters using CSS Unicode escaping. Just as JavaScript string
contexts are often in a parent `<script>` tag, CSS contexts often have a parent
HTML context within the `<style>` tag. For the same reason as JavaScript, we also
escape the `/` character.

### URIs

The URI context is comprised of numerous sub-contexts. [RFC 3986] [8] provides
details on each of them. When used in HTML, the URL context includes some parent context, 
such as HTML, JavaScript, or CSS.

Injection examples:
```html
<style>
    #clickme a {
      background-image: url('TAINTED_DATA_HERE');
    }
</style>
<a id="clickme" href="http://www.example.com/?test=TAINTED_DATA_HERE">Click me!</a>
```
When the tainted data is inserted as a query parameter, the Escape library
meets the URI query parameter obligations by encoding sensitive characters using
URI percent encoding.

### SQL LIKE Context

SQL LIKE clauses use special characters to perform wildcard matching. When 
tainted data is used within a SQL LIKE clause, even is passed via a named
parameter, the tainted data should have these wildcards escaped. The escaping
preserves the intent of the wild cards in the LIKE clause. For example, if 
only a trailing percent sign (%, U+0025) is used in the clause, then if the 
tainted data included a leading percent sign, more results could possibly be
returned, changing the intent of the query.

Injection example:

```java
entityManager.createQuery("FROM MyEntity e WHERE e.content LIKE :like_query")
             .setParameter("like_query", "%" + TAINTED_DATA_HERE)
             .getResultList();
```

The Escape library meets these obligations by escaping these wildcard characters
using an additional escape character, by default the at sign (@, U+0040):

```java
entityManager.createQuery("FROM MyEntity e WHERE e.content LIKE :like_query ESCAPE '@'")
             .setParameter("like_query", "%" + Escape.sqlLikeClause(TAINTED_DATA_HERE))
             .getResultList();
```

Note: the Escape library does not prevent SQL injection issues. It preserves
the meaning of the LIKE query by escaping only characters with special meaning
in a LIKE clause.

### Unquoted 

HTML allows attribute values and CSS allows URI values to be used in an unquoted contexts, along
with their single and double-quoted alternatives. We recommend not using the 
unquoted context in HTML or CSS. Rather, use the double-quoted context. The reasoning
is that unquoted contexts make it even more difficult to mitigate and are sometimes
web browser specific.

# <a id="main_authors"></a> Authors
The Escape library was developed by the [Coverity Security Research Lab](http://www.coverity.com) members:
* Romain Gaucher, [@rgaucher](https://twitter.com/rgaucher)
* Andy Chou, [@_achou](https://twitter.com/_achou)
* Jon Passki, [@jonpasski](https://twitter.com/jonpasski)

# License
    Copyright (c) 2012, Coverity, Inc. 
    All rights reserved.

    Redistribution and use in source and binary forms, with or without modification, 
    are permitted provided that the following conditions are met:
    - Redistributions of source code must retain the above copyright notice, this 
    list of conditions and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this
    list of conditions and the following disclaimer in the documentation and/or other
    materials provided with the distribution.
    - Neither the name of Coverity, Inc. nor the names of its contributors may be used
    to endorse or promote products derived from this software without specific prior 
    written permission from Coverity, Inc.
    
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND INFRINGEMENT ARE DISCLAIMED.
    IN NO EVENT SHALL THE COPYRIGHT HOLDER OR  CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
    WHETHER IN CONTRACT,  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
    OF SUCH DAMAGE.


[1]: http://www.whatwg.org/specs/web-apps/current-work/#syntax-ambiguous-ampersand "Ambiguous ampersand"
[2]: http://www.ecma-international.org/publications/standards/Ecma-262.htm "ECMAScript language"
[3]: http://www.w3.org/TR/CSS2/syndata.html#strings "CSS strings"
[4]: http://www.whatwg.org/specs/web-apps/current-work/multipage/syntax.html#rcdata-elements "RCDATA"
[5]: http://www.whatwg.org/specs/web-apps/current-work/multipage/syntax.html#normal-elements "normal element"
[6]: http://www.whatwg.org/specs/web-apps/current-work/multipage/syntax.html#attributes-0 "single and double-quoted attribute"
[7]: http://www.whatwg.org/specs/web-apps/current-work/multipage/syntax.html#syntax-comments "HTML comments"
[8]: http://tools.ietf.org/html/rfc3986 "RFC 3986"

