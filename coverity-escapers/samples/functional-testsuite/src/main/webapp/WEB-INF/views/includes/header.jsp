<%@ include file="/WEB-INF/views/includes/taglibs.jsp" %>
<!doctype html>
<html>
<head>
    <title>CSL test suite - JSP</title>
    <link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/css/bootstrap-combined.min.css" rel="stylesheet">
    <link href="//netdna.bootstrapcdn.com/font-awesome/2.0/css/font-awesome.css" rel="stylesheet">
    
    <style>
/* shamelessly copied from bootstrap website */
body {
  position: relative;
  padding-top: 60px;
}
section {
  padding-top: 30px;
}
section > .page-header,
section > .lead {
  color: #5a5a5a;
}
section > ul li {
  margin-bottom: 5px;
}
.bs-docs-sidenav {
  width: 228px;
  margin: 30px 0 0;
  padding: 0;
  background-color: #fff;
  -webkit-border-radius: 6px;
     -moz-border-radius: 6px;
          border-radius: 6px;
  -webkit-box-shadow: 0 1px 4px rgba(0,0,0,.065);
     -moz-box-shadow: 0 1px 4px rgba(0,0,0,.065);
          box-shadow: 0 1px 4px rgba(0,0,0,.065);
}
.bs-docs-sidenav > li > a {
  display: block;
  *width: 190px;
  margin: 0 0 -1px;
  padding: 8px 14px;
  border: 1px solid #e5e5e5;
}
.bs-docs-sidenav > li:first-child > a {
  -webkit-border-radius: 6px 6px 0 0;
     -moz-border-radius: 6px 6px 0 0;
          border-radius: 6px 6px 0 0;
}
.bs-docs-sidenav > li:last-child > a {
  -webkit-border-radius: 0 0 6px 6px;
     -moz-border-radius: 0 0 6px 6px;
          border-radius: 0 0 6px 6px;
}
.bs-docs-sidenav > .active > a {
  position: relative;
  z-index: 2;
  padding: 9px 15px;
  border: 0;
  text-shadow: 0 1px 0 rgba(0,0,0,.15);
  -webkit-box-shadow: inset 1px 0 0 rgba(0,0,0,.1), inset -1px 0 0 rgba(0,0,0,.1);
     -moz-box-shadow: inset 1px 0 0 rgba(0,0,0,.1), inset -1px 0 0 rgba(0,0,0,.1);
          box-shadow: inset 1px 0 0 rgba(0,0,0,.1), inset -1px 0 0 rgba(0,0,0,.1);
}
/* Chevrons */
.bs-docs-sidenav .icon-chevron-right {
  float: right;
  margin-top: 2px;
  margin-right: -6px;
  opacity: .25;
}
.bs-docs-sidenav > li > a:hover {
  background-color: #f5f5f5;
}
.bs-docs-sidenav a:hover .icon-chevron-right {
  opacity: .5;
}
.bs-docs-sidenav .active .icon-chevron-right,
.bs-docs-sidenav .active a:hover .icon-chevron-right {
  opacity: 1;
}
.bs-docs-sidenav.affix {
  top: 40px;
}
.bs-docs-sidenav.affix-bottom {
  position: absolute;
  top: auto;
  bottom: 270px;
}

.com { color: #93a1a1; }
.lit { color: #195f91; }
.pun, .opn, .clo { color: #93a1a1; }
.fun { color: #dc322f; }
.str, .atv { color: #D14; }
.kwd, .prettyprint .tag { color: #1e347b; }
.typ, .atn, .dec, .var { color: teal; }
.pln { color: #48484c; }
 
.prettyprint {
  padding: 8px;
  background-color: #f7f7f9;
  border: 1px solid #e1e1e8;
}
.prettyprint.linenums {
  -webkit-box-shadow: inset 40px 0 0 #fbfbfc, inset 41px 0 0 #ececf0;
     -moz-box-shadow: inset 40px 0 0 #fbfbfc, inset 41px 0 0 #ececf0;
          box-shadow: inset 40px 0 0 #fbfbfc, inset 41px 0 0 #ececf0;
}
 
/* Specify class=linenums on a pre to get line numbering */
ol.linenums {
  margin: 0 0 0 33px; /* IE indents via margin-left */
}
ol.linenums li {
  padding-left: 12px;
  color: #bebec5;
  line-height: 20px;
  text-shadow: 0 1px 0 #fff;
}
    </style>
</head>

<body data-spy="scroll" data-target=".bs-docs-sidebar" onload="prettyPrint()">

<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="https://github.com/coverity/coverity-security-library">CSL on Github</a>
      <div class="nav-collapse collapse">
        <ul class="nav">
          <li>
            <a href="/testsuite/scriptlet">Java/JSP test suite</a>
          </li>
          <li>
            <a href="/testsuite/el">EL test suite</a>
          </li>
        </ul>
      </div>
    </div>
  </div>
</div>

<div class='container-fluid'>
<div class="row-fluid">