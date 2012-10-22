<%@ page import="com.coverity.security.Escape" %>

<%@ include file="/WEB-INF/views/includes/header.jsp" %>

      <div class="span3 bs-docs-sidebar">
        <ul class="nav nav-list bs-docs-sidenav affix">
            <li><a href="#html"><i class="icon-chevron-right"></i> Escape.html</a></li>
            <li><a href="#jsString"><i class="icon-chevron-right"></i> Escape.jsString</a></li>
            <li><a href="#cssString"><i class="icon-chevron-right"></i> Escape.cssString</a></li>
            <li><a href="#jsRegex"><i class="icon-chevron-right"></i> Escape.jsRegex</a></li>
            <li><a href="#uri"><i class="icon-chevron-right"></i> Escape.uri</a></li>
            <li><a href="#nested"><i class="icon-chevron-right"></i> Nested contexts</a></li>
        </ul>
      </div>

      <div class="span9">

<section id="html">
	<div class="page-header">
	<h1>html <small>com.coverity.security.Escape.html</small></h1>
	</div>
	<ul>
		<li>Performs HTML escaping from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;%
    String input = request.getParameter("web");
    String result = Escape.html(input);
	
    out.write("&lt;div>");
    out.write("Content:" + result);
    out.write("&lt;/div>");
%&gt;
</pre>
	<h3>Result</h3>
<%
String input = request.getParameter("web");
String result = Escape.html(input);

out.write("<div>");
out.write("Content:" + result);
out.write("</div>");
%>
</section>

<section id="html">
	<div class="page-header">
	<h1>html <small>com.coverity.security.Escape.html</small></h1>
	</div>
	<ul>
		<li>Performs HTML escaping from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;% 
    String input2 = request.getParameter("web");
    
    out.write("&lt;div id=\"");
    out.write(Escape.html(input2));
    out.write("\">\n");
    out.write("Content");
    out.write("&lt;/div>");
%&gt;
</pre>
	<h3>Result</h3>
<% 
	String input2 = request.getParameter("web");

	out.write("<div id=\"");
	out.write(Escape.html(input2));
	out.write("\">\n");
	out.write("Content");
	out.write("</div>");
%>
</section>


<section id="jsString">
	<div class="page-header">
	<h1>jsString <small>com.coverity.security.Escape.jsString</small></h1>
	</div>
	<ul>
		<li>Performs JavaScript String Unicode escaping from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;script type="text/javascript">
    var x = '&lt;%= Escape.jsString(request.getParameter("web")) %&gt;'; 
    console.log(x);
&lt;/script>
</pre>
	<h3>Result</h3>
	<script type="text/javascript">
		var x = '<%= Escape.jsString(request.getParameter("web")) %>'; 
		console.log(x);
	</script>
</section>

<section id="jsString">
	<div class="page-header">
	<h1>jsString <small>com.coverity.security.Escape.jsString</small></h1>
	</div>
	<ul>
		<li>Performs JavaScript String Unicode escaping from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;script type="text/javascript">
    var x = "&lt;%= Escape.jsString(request.getParameter("web")) %&gt;"; 
    console.log(x);
&lt;/script>
</pre>
	<h3>Result</h3>
	<script type="text/javascript">
		var x = "<%= Escape.jsString(request.getParameter("web")) %>"; 
		console.log(x);
	</script>
</section>

<section id="cssString">
	<div class="page-header">
	<h1>cssString <small>com.coverity.security.Escape.cssString</small></h1>
	</div>
	<ul>
		<li>Performs CSS String escaping from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;style>
    div[id *= '&lt;%= Escape.cssString(request.getParameter("web")) %&gt;'] { 
        background-color: pink;
    }
&lt;/style>
</pre>
	<h3>Result</h3>
	<style>
		div[id *= '<%= Escape.cssString(request.getParameter("web")) %>'] { 
			background-color: pink;
		}
	</style>
	<div id="test-result">test-content</div>
</section>

<section id="cssString">
	<div class="page-header">
	<h1>cssString <small>com.coverity.security.Escape.cssString</small></h1>
	</div>
	<ul>
		<li>Performs CSS String escaping from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;style>
    div[id *= "&lt;%= Escape.cssString(request.getParameter("web")) %&gt;"] { 
        background-color: pink;
    }
&lt;/style>
</pre>
	<h3>Result</h3>
	<style>
		div[id *= "<%= Escape.cssString(request.getParameter("web")) %>"] { 
			background-color: pink;
		}
	</style>
	<div id="test-result">test-content</div>
</section>

<section id="jsRegex">
	<div class="page-header">
	<h1>jsRegex <small>com.coverity.security.Escape.jsRegex</small></h1>
	</div>
	<ul>
		<li>Performs JavaScript regex escaping from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;script type="text/javascript">
    var control = 'content';
    var reg = /^&lt;%= Escape.jsRegex(request.getParameter("web")) %&gt;/;
    console.log(reg.test(control));
&lt;/script>
</pre>
	<h3>Result</h3>
	<script type="text/javascript">
		var control = 'content';
		var reg = /^<%= Escape.jsRegex(request.getParameter("web")) %>/;
		console.log(reg.test(control));
	</script>
</section>

<section id="uri">
	<div class="page-header">
	<h1>uri <small>com.coverity.security.Escape.uri</small></h1>
	</div>
	<ul>
		<li>Performs URI encoding from Java or JSP scriptlet</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;a href="/testsuite/el?web=&lt;%= Escape.uri(request.getParameter("web")) %&gt;">web link&lt;/a>
</pre>
	<h3>Result</h3>
	<a href="/testsuite/el?web=<%= Escape.uri(request.getParameter("web")) %>">web link</a>
</section>


<!-- Then, we can add some nested context variations -->
<section id="nested">
	<div class="page-header">
	<h1>Nested contexts <small>HTML double quoted attribute, JavaScript single-quoted string</small></h1>
	</div>
	<ul>
		<li>Uses Escape.jsString and Escape.html</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;%
    String inputNested1 = request.getParameter("web");
    String hjString1 = Escape.html(Escape.jsString(inputNested1));
%&gt;
&lt;img src=. onerror="console.log('&lt;%= hjString1 %&gt;')" /&gt;
</pre>
	<h3>Result</h3>
<%
String inputNested1 = request.getParameter("web");
String hjString1 = Escape.html(Escape.jsString(inputNested1));
%>
<img src=. onerror="console.log('<%= hjString1 %>')" />
	
	<div class='well well-small'>
	Note, here's what the JavaScript engine receives:
	<code style="background-color:#fff">console.log('<%= hjString1 %>')</code>
	</div>
	
</section>


<section id="nested">
	<div class="page-header">
	<h1>Nested contexts <small>HTML double quoted attribute, CSS single-quoted string (in url())</small></h1>
	</div>
	<ul>
		<li>Uses Escape.cssString and Escape.html</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;%
    String inputNested2 = request.getParameter("web");
    String chString1 = Escape.html(Escape.cssString(inputNested2));
%&gt;
&lt;div style="background-image: url('/404/&lt;%= chString1 %&gt;')">
    Content
&lt;/div>
</pre>
	<h3>Result</h3>
<%
String inputNested2 = request.getParameter("web");
String chString1 = Escape.html(Escape.cssString(inputNested2));
%>
	<div style="background-image: url('/404/<%= chString1 %>')">
	Content
	</div>

	<div class='well well-small'>
	Note, here's what the CSS engine receives:
	<code style="background-color:#fff">background-image: url('/404/<%= chString1 %>')</code>
	</div>
</section>

<section id="nested">
	<div class="page-header">
	<h1>Nested contexts <small>JavaScript String, JavaScript regex</small></h1>
	</div>
	<ul>
		<li>Uses Escape.jsRegex and Escape.jsString</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;%
    String inputNested3 = request.getParameter("web");
    String jjString1 = Escape.jsString(Escape.jsRegex(inputNested3));
%&gt;
&lt;script type="text/javascript">
	var control = 'content';
	var reg = new RegExp('^(&lt;%= jjString1 %&gt;)?content'); 
	console.log(reg.test(control));
&lt;/script>
</pre>
	<h3>Result</h3>
<%
String inputNested3 = request.getParameter("web");
String jjString1 = Escape.jsString(Escape.jsRegex(inputNested3));
%>
	<script type="text/javascript">
		var control = 'content';
		var reg = new RegExp('^(<%= jjString1 %>)?content'); 
		console.log(reg.test(control));
	</script>
	<div class='well well-small'>
	Note, here's what the JS engine receives:
	<code style="background-color:#fff">var reg = new RegExp('^(<%= Escape.html(jjString1) %>)?content');</code>
	</div>
</section>

	</div>

<%@ include file="/WEB-INF/views/includes/footer.jsp" %>