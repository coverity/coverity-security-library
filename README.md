# Coverity Security Library
[Coverity](http://www.coverity.com) [Security Research Labs](https://communities.coverity.com/blogs/security) has developed open source (BSD style) Java libraries useful to remedy security defects. 

## Escape

The [Escape library](https://github.com/coverity/coverity-security-library/coverity-escapers) contains several escapers for web content. These escaping functions help remedy common defects (mostly cross-site scripting) that occur when the data is inserted into HTML element, HTML attribute values, URI, JavaScript strings, SQL LIKE clauses, etc. More information are available in the [Escape sub directory](https://github.com/coverity/coverity-security-library/coverity-escapers).

Before using any of these methods, the user should understand the context (or nested contexts) in which the data is inserted. Some examples are available in the [repository](https://github.com/coverity/coverity-security-library/coverity-escapers/samples), and more will be available on [our blog](https://communities.coverity.com/blogs/security).

To include this library into your Maven project, add the following:

    <dependency>
        <groupId>com.coverity.security</groupId>
        <artifactId>coverity-escapers/artifactId>
        <version>1.0.0</version>
    </dependency>

To contact the SRL, please email us at <srl@coverity.com>. Fork away, we look forward to your pull requests!

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