[![Build Status](https://travis-ci.org/coverity/coverity-security-library.png?branch=develop)](https://travis-ci.org/coverity/coverity-security-library)

# Coverity Security Library
The Coverity Security Library (CSL) is a lightweight set of utilities for fixing cross-site scripting (XSS), SQL injection, and other security defects in Java web applications.

Here's why it's worth checking out:

* **It's secure:** We take the security of CSL seriously. Every change is carefully scrutinized through a process that includes manual code review, static analysis, fuzz testing, and unit testing.

* **It's convenient:** CSL contains escapers for XSS and SQL injection that are missing from standard libraries like Apache Commons and Java EE.  We use fast, easy to invoke static methods with short, intuitive names.  We also provide hooks for Expression Language (EL) to make it easy to use within JSPs. CSL also contains enhanced templating libraries for SQL statements and filesystem paths which work as drop-in replacements that allow you to safely specify dynamic values like table names or folder/file names.

* **It's small:** CSL has no external runtime dependencies and is a minimalist library. This means it's fast and does not require any configuration besides dropping a JAR in the right location or modifying your build to do it.

* **It's free:** CSL is distributed under a BSD-style license.  We would appreciate patches be sent back to us but it's not required.

Users of Coverity Security Advisor get remediation guidance based on escaping routines in CSL.  However, CSL is a standalone project with no dependencies on Security Advisor.


## Escape

The [Escape class](https://github.com/coverity/coverity-security-library/tree/develop/coverity-escapers) contains several escapers for web content. These escaping functions help remedy common defects (mostly cross-site scripting) that occur when the data is inserted into HTML element, HTML attribute values, URI, JavaScript strings, SQL LIKE clauses, etc. More information are available in the [Escape directory](https://github.com/coverity/coverity-security-library/tree/develop/coverity-escapers).

Before using any of these methods, you should understand the context (or nested contexts) in which the data is inserted. [Several mockup examples with explanation](https://github.com/coverity/coverity-security-library/tree/develop/coverity-escapers/samples/mockup-examples) are available in the repository, and more will be available on [our blog](https://communities.coverity.com/blogs/security). 
If you want to test the library to understand how it whistands security attacks, our [functional testsuite](https://github.com/coverity/coverity-security-library/tree/develop/coverity-escapers/samples/functional-testsuite) is the right app to build/deploy/test.

Ready to use it? One last step is to have a look at [the latest javadoc](http://coverity.github.com/coverity-security-library) directly on github.

To include this library into your Maven project, add the following:

```xml
<dependency>
    <groupId>com.coverity.security</groupId>
    <artifactId>coverity-escapers</artifactId>
    <version>1.1.1</version>
</dependency>
```

or drop the JAR file in the <code>WEB-INF/lib</code> directory.

Then you can use it directly in your JSPs:

```jsp
<%@ taglib uri="http://coverity.com/security" prefix="cov" %>
<script type="text/javascript">
    var x = '${cov:jsStringEscape(param.tainted)}';
</script>
<div onclick="alert('${cov:htmlEscape(cov:jsStringEscape(param.tainted))}')">
    ${cov:htmlEscape(param.tainted)}
</div>
```

or in your Java programs:

```java
import com.coverity.security.Escape;
// ...
return "<div onclick='alert(\"" 
       + Escape.html(Escape.jsString(request.getParameter("tainted")))
       + "\")'>" 
       + Escape.html(request.getParameter("tainted")) 
       + "</div>";
```

To contact the SRL, please email us at <srl@coverity.com>. Fork away, we look forward to your pull requests!


## Templates

The coverity-templates module contains APIs for safely specifying dynamic values in SQL and filesystem path contexts.
For example, if you have a piece of code which uses a user-controllable value for the ORDER BY clause, you may have
code that looks like this:

```java
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM foo WHERE x=? ORDER BY " + orderBy);
stmt.setInteger(1, 100);
ResultSet rs = stmt.executeQuery();
// ...
```

With the EnhancedPreparedStatement class, you can set identifiers in addition to data values:

```java
EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(conn, "SELECT * FROM foo WHERE x=? ORDER BY ?");
stmt.setInteger(1, 100);
stmt.setIdentifier(2, orderBy);
ResultSet rs = stmt.executeQuery();
// ...
```

The library is able to use the JDBC metadata to safely validate and quote identifiers no matter what SQL backend you're
using, so an attacker able to modify the value of `orderBy` would be unable to change the intent of this SQL query.

The module also contains an API for safely specifying filesystem paths. Consider the following code for fetching uploaded
files:

```java
File uploadRoot = new File("/home/tomcat/userdata/uploads");
File file = new File(uploadRoot, request.getParameter("filename"));
IOUtils.copy(new FileInputStream(file), request.getOutputStream());
```

By specifying `"../../../../etc/passwd"` an attacker could read the `/etc/passwd` file from the server. Blacklisting
`File.separatorChar` is insufficient since some platforms support multiple path separator characters (like Windows, which
supports both `\` and `/`).

The `SimplePath` API understands all of the platform's path separators as well as upward traversal patterns, and checks
the input against upward traversal. The above code could be rewritten as:

```java
SimplePath uploadRoot = new SimplePath("/home/tomcat/userdata/uploads");
SimplePath file = uploadRoot.path(request.getParameter("filename"));
IOUtils.copy(new FileInputStream(file), request.getOutputStream());
```

With this code, an attacker can specify any path as long as it doesn't escape the `/home/tomcat/userdata/uploads`
directory. So the call to `uploadRoot.path()` would through an exception on `"../../../../etc/passwd"` but would allow
`"foo/bar/../baz.txt"`.

### TrustySimplePath and @TrustedPath

The coverity-templates module contains the `TrustySimplePath` which extends `SimplePath` and the `@TrustedPath`
annotation which can be used to aid Coverity's static analysis. Their use is optional, but may help you reduce the
incidence of false positives when using Coverity's Secure Coding Practices checkers. The `TrustySimplePath` class
will implicitly trust data coming from Java system properties and environment variables so that code such as

```java
new TrustySimplePath(System.getProperty("java.io.tmpdir"));
new TrustySimplePath(System.getenv("HOME"));
```

doesn't trigger warnings about using dynamic data in filesystem sinks. The `@TrustedPath` can be used to whitelist
other dynamic data sources for either `TrustySimplePath` or for `SimplePath`.

```java
public File getUploadedFile(String path) {
    return new SimplePath(getUserUploadRoot()).path(path);
}
@TrustedPath
public void getUserUploadRoot() {
    return configProperties.getProperty("user_upload_root");
}
```

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
