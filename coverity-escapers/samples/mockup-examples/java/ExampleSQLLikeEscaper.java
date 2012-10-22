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
package com.example.blog.data.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

// Import the Coverity escapers
import com.coverity.security.Escape;

import com.example.blog.data.domain.BlogEntry;

// Just an example DAO object that helps manage domain 
// objects, and uses JPA.
@Repository
public class ExampleSQLLikeEscaper {

    @PersistenceContext
    private EntityManager entityManager;
    
    public BlogEntry save(BlogEntry blogEntry) {
        this.entityManager.persist(blogEntry);
        return blogEntry;
    }
    
    public BlogEntry update(BlogEntry blogEntry) {
        return this.save(blogEntry);
    }
    
    @SuppressWarnings("unchecked")
    public List<BlogEntry> find(String title) {
        
        // The `sqlLikeClause` escaper does not protect against SQL (or HQL/JPQL) injection,
        // but makes the LIKE clause correct; the user cannot change the meaning of the
        // % and _ directives.
        //
        // In this example, we use JPQL and named parameters to supply the tainted value
        // `title`, escape it for the LIKE clause, and append it in our parameter binding
        // with `setParameter`.
        // Note that you should specify what escape character is used. In this case, we 
        // also set it using a named parameter `escape_char` with the ESCAPE directive.
        //
        // The `sqlLikeClause` method can take a second parameter which specify what 
        // escape character is to be used. However, we privide s simpler interface that
        // sets this escape character to '@' as this seems to be the most compatible
        // with different databases.
        return this.entityManager.createQuery("from BlogEntry b where b.title like :title"
                                            + " order by a.title escape :escape_char")
                                 .setParameter("title", Escape.sqlLikeClause(title) + "%")
                                 .setParameter("escape_char", "@")
                                 .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<BlogEntry> all() {
        return this.entityManager.createQuery("select * from BlogEntry").getResultList();
    }
}
