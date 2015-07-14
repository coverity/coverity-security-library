package com.coverity.security.path;

import java.io.File;
import java.net.URI;

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

    public TrustySimplePath(File file) {
        super(file);
    }

    public TrustySimplePath(String parent, String child) {
        super(parent, child);
    }

    public TrustySimplePath(File parent, String child) {
        super(parent, child);
    }

    public TrustySimplePath(String parent, String[] children) {
        super(parent, children);
    }

    public TrustySimplePath(File parent, String[] children) {
        super(parent, children);
    }

    public TrustySimplePath(URI uri) {
        super(uri);
    }
}
