package com.coverity.security.path;

/**
 * TrustySimplePath is a class with the same API and behavior as SimplePath. It is provided in order to allow for
 * indicating alternative semantics for Coverity's static analysis. See Coverity's blog post for more information:
 *
 *   // TODO: Add blog post link
 *
 * @see SimplePath
 */
public class TrustySimplePath extends SimplePath {
    public TrustySimplePath(String pathname) {
        super(pathname);
    }
}
