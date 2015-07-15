package com.coverity.security.path;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Provides an interface for accessing arbitrary filesystem paths while transparently enforcing those paths to reside
 * within the provided root directory. The root directory is determined by the path supplied to the, constructor with
 * subdirectory/file traversal made possible through the sub() method. For example:
 *
 *   RootedPath HOME_ROOT = new RootedPath("/home");
 *   RootedPath usersHome = HOME_ROOT.sub("username");
 *
 * Attempts to traverse to a directory outside the root (e.g. through upwards traversals) will result in an exception
 * being thrown. For example:
 *
 *   usersHome.sub("foo/bar/../../baz"); // Resolves to "/home/username/baz"; does not throw an exception
 *   usersHome.sub("foo/../../baz"); // Resolves to "/home/baz"; throws an exception
 *
 */
public class RootedPath extends File {

    /**
     * Constructs a new RootedPath using the String as the root directory.
     * @param root The root path to use for this instance.
     */
    public RootedPath(String root) {
        super(root);
    }

    /**
     * Constructs a new RootedPath using the File as the root directory.
     * @param root The root path to use for this instance.
     */
    public RootedPath(File root) {
        super(root.getPath());
    }

    /**
     * Constructs a new RootedPath using the URI as the root directory.
     * @param root The root path to use for this instance.
     */
    public RootedPath(URI root) {
        super(root);
    }

    /**
     * Constructs a new RootedPath instance representing the subdirectory or file represented by the child
     * parameter. Will throw an exception if the child parameter represents a path outside of this RootedPath.
     *
     * @param child The subdirectory or descendant file the new RootedPath will represent
     * @return A RootedPath representing the subdirectory.
     */
    public RootedPath sub(final String child) {
        if (child.equals("") || child.equals(".")) {
            return this;
        }
        final File subDir = new File(this, child);
        validate(this, subDir);
        return new RootedPath(subDir);
    }

    @Override
    public RootedPath getParentFile() {
        File parent = super.getParentFile();
        return (parent == null ? null : new RootedPath(parent));
    }

    private static void validate(File root, File subDir) {
        File thisPath;
        File rootPath;
        try {
            thisPath = subDir.getCanonicalFile();
            rootPath = root.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (thisPath != null) {
            if (thisPath.equals(rootPath)) {
                return;
            }
            thisPath = thisPath.getParentFile();
        }
        throw new IllegalArgumentException("Child is not contained within the tree's root.");
    }

}