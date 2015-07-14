package com.coverity.security.path;

import java.io.File;
import java.net.URI;

/**
 * TrustyRootedPath is a class with the same API and behavior as RootedPath. It is provided in order to allow for
 * indicating alternative semantics for Coverity's static analysis. See Coverity's blog post for more information:
 *
 *   // TODO: Add blog post link
 *
 * @see RootedPath
 */
public class TrustyRootedPath extends RootedPath {
    public TrustyRootedPath(String root) {
        super(root);
    }

    public TrustyRootedPath(File root) {
        super(root);
    }

    public TrustyRootedPath(URI root) {
        super(root);
    }
}
