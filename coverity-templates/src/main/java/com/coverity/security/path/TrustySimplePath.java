package com.coverity.security.path;

/**
 * <code>TrustySimplePath</code> is a class with the same API and behavior as {@link SimplePath}. It is provided in
 * order to allow for indicating alternative semantics for Coverity's static analysis. See the Coverity Security Library
 * <a href="https://github.com/coverity/coverity-security-library/blob/develop/README.md">README</a> for more information.
 *
 * @see SimplePath
 */
public class TrustySimplePath extends SimplePath {
    public TrustySimplePath(String pathname) {
        super(pathname);
    }
}
