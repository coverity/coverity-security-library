<%@ include file="/WEB-INF/views/includes/header.jsp" %>

      <div class="span3 bs-docs-sidebar">
        <ul class="nav nav-list bs-docs-sidenav affix">
            <li><a href="#htmlEscape"><i class="icon-chevron-right"></i> htmlEscape</a></li>
            <li><a href="#jsStringEscape"><i class="icon-chevron-right"></i> jsStringEscape</a></li>
            <li><a href="#cssStringEscape"><i class="icon-chevron-right"></i> cssStringEscape</a></li>
            <li><a href="#jsRegexEscape"><i class="icon-chevron-right"></i> jsRegexEscape</a></li>
            <li><a href="#uriEncode"><i class="icon-chevron-right"></i> uriEncode</a></li>
            <li><a href="#nested"><i class="icon-chevron-right"></i> Nested contexts</a></li>
        </ul>
      </div>

      <div class="span9">

<section id="htmlEscape">
	<div class="page-header">
	<h1>htmlEscape <small>com.coverity.security.EscapeEL.htmlEscape</small></h1>
	</div>
	<ul>
		<li>Performs HTML escaping as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;div> 
    Content: \${cov:htmlEscape(param.web)}
&lt;/div>
</pre>
	<h3>Result</h3>
	<div> 
	Content: ${cov:htmlEscape(param.web)}
	</div>
</section>

<section id="htmlEscape">
	<div class="page-header">
	<h1>htmlEscape <small>com.coverity.security.EscapeEL.htmlEscape</small></h1>
	</div>
	<ul>
		<li>Performs HTML escaping as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;div id="\${cov:htmlEscape(param.web)}"> 
    Content
&lt;/div>
</pre>
	<h3>Result</h3>
	<div id="${cov:htmlEscape(param.web)}"> 
	Content
	</div>
</section>


<section id="jsStringEscape">
	<div class="page-header">
	<h1>jsStringEscape <small>com.coverity.security.EscapeEL.jsStringEscape</small></h1>
	</div>
	<ul>
		<li>Performs JavaScript String Unicode escaping as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;script type="text/javascript">
    var x = '\${cov:jsStringEscape(param.web)}'; 
    console.log(x);
&lt;/script>
</pre>
	<h3>Result</h3>
	<script type="text/javascript">
		var x = '${cov:jsStringEscape(param.web)}'; 
		console.log(x);
	</script>
</section>

<section id="jsStringEscape">
	<div class="page-header">
	<h1>jsStringEscape <small>com.coverity.security.EscapeEL.jsStringEscape</small></h1>
	</div>
	<ul>
		<li>Performs JavaScript String Unicode escaping as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;script type="text/javascript">
    var x = "\${cov:jsStringEscape(param.web)}"; 
    console.log(x);
&lt;/script>
</pre>
	<h3>Result</h3>
	<script type="text/javascript">
		var x = "${cov:jsStringEscape(param.web)}"; 
		console.log(x);
	</script>
</section>

<section id="cssStringEscape">
	<div class="page-header">
	<h1>cssStringEscape <small>com.coverity.security.EscapeEL.cssStringEscape</small></h1>
	</div>
	<ul>
		<li>Performs CSS String escaping as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;style>
    div[id *= '\${cov:cssStringEscape(param.web)}'] { 
        background-color: pink;
    }
&lt;/style>
</pre>
	<h3>Result</h3>
	<style>
		div[id *= '${cov:cssStringEscape(param.web)}'] { 
			background-color: pink;
		}
	</style>
	<div id="test-result">test-content</div>
</section>

<section id="cssStringEscape">
	<div class="page-header">
	<h1>cssStringEscape <small>com.coverity.security.EscapeEL.cssStringEscape</small></h1>
	</div>
	<ul>
		<li>Performs CSS String escaping as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;style>
    div[id *= "\${cov:cssStringEscape(param.web)}"] { 
        background-color: pink;
    }
&lt;/style>
</pre>
	<h3>Result</h3>
	<style>
		div[id *= "${cov:cssStringEscape(param.web)}"] { 
			background-color: pink;
		}
	</style>
	<div id="test-result">test-content</div>
</section>

<section id="jsRegexEscape">
	<div class="page-header">
	<h1>jsRegexEscape <small>com.coverity.security.EscapeEL.jsRegexEscape</small></h1>
	</div>
	<ul>
		<li>Performs JavaScript regex escaping as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;script type="text/javascript">
    var control = 'content';
    var reg = /^\${cov:jsRegexEscape(param.web)}/;
    console.log(reg.test(control));
&lt;/script>
</pre>
	<h3>Result</h3>
	<script type="text/javascript">
		var control = 'content';
		var reg = /^${cov:jsRegexEscape(param.web)}/;
		console.log(reg.test(control));
	</script>
</section>

<section id="uriEncode">
	<div class="page-header">
	<h1>uriEncode <small>com.coverity.security.EscapeEL.uriEncode</small></h1>
	</div>
	<ul>
		<li>Performs URI encoding as EL function</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;a href="/testsuite/el?web=\${cov:uriEncode(param.web)}">web link&lt;/a>
</pre>
	<h3>Result</h3>
	<a href="/testsuite/el?web=${cov:uriEncode(param.web)}">web link</a>
</section>


<!-- Then, we can add some nested context variations -->
<section id="nested">
	<div class="page-header">
	<h1>Nested contexts <small>HTML double quoted attribute, JavaScript single-quoted string</small></h1>
	</div>
	<ul>
		<li>Uses jsStringEscape and htmlEscape</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;img src=. onerror="console.log('\${cov:htmlEscape(cov:jsStringEscape(param.web))}')" />
</pre>
	<h3>Result</h3>
	
	<img src=. onerror="console.log('${cov:htmlEscape(cov:jsStringEscape(param.web))}')" />
	
	<div class='well well-small'>
	Note, here's what the JavaScript engine receives:
	<code style="background-color:#fff">console.log('${cov:htmlEscape(cov:jsStringEscape(param.web))}')</code>
	</div>
	
</section>


<section id="nested">
	<div class="page-header">
	<h1>Nested contexts <small>HTML double quoted attribute, CSS single-quoted string (in url())</small></h1>
	</div>
	<ul>
		<li>Uses cssStringEscape and htmlEscape</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;div style="background-image: url('/404/\${cov:htmlEscape(cov:cssStringEscape(param.web))}')">
    Content
&lt;/div>
</pre>
	<h3>Result</h3>
	<div style="background-image: url('/404/${cov:htmlEscape(cov:cssStringEscape(param.web))}')">
	Content
	</div>

	<div class='well well-small'>
	Note, here's what the CSS engine receives:
	<code style="background-color:#fff">background-image: url('/404/${cov:htmlEscape(cov:cssStringEscape(param.web))}')</code>
	</div>
</section>

<section id="nested">
	<div class="page-header">
	<h1>Nested contexts <small>JavaScript String, JavaScript regex</small></h1>
	</div>
	<ul>
		<li>Uses jsRegexEscape and jsStringEscape</li>
		<li>Testcases: GET parameter 'web'</li>
	</ul>
	<h3>Code</h3>
<pre class="prettyprint linenums">
&lt;script type="text/javascript">
    var control = 'content';
    var reg = new RegExp('^(\${cov:jsStringEscape(cov:jsRegexEscape(param.web))})?content'); 
    console.log(reg.test(control));
&lt;/script>
</pre>
	<h3>Result</h3>
	<script type="text/javascript">
		var control = 'content';
		var reg = new RegExp('^(${cov:jsStringEscape(cov:jsRegexEscape(param.web))})?content'); 
		console.log(reg.test(control));
	</script>
	<div class='well well-small'>
	Note, here's what the JS engine receives:
	<code style="background-color:#fff">var reg = new RegExp('^(${cov:htmlEscape(cov:jsStringEscape(cov:jsRegexEscape(param.web)))})?content');</code>
	</div>
</section>

	</div>
<%@ include file="/WEB-INF/views/includes/footer.jsp" %>