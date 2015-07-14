package com.coverity.security.path;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Provides an interface for accessing arbitrary filesystem paths while transparently enforcing those paths to be
 * contained within within a provided root directory. The root directory is determined by the path supplied to the
 * constructor, which subdirectory traversal made possible through the sub() method. For example:
 *
 *   RootedPath HOME_ROOT = new RootedPath("/home");
 *   RootedPath usersHome = HOME_ROOT.sub("username");
 *
 * If it is possible to relocate a root to a subdirectory using the chroot() method:
 *
 *   usersHome = usersHome.chroot();
 *
 * Attempts to traverse to a directory outside the root (e.g. through upwards traversals) will result in an exception
 * being thrown. For example:
 *
 *   usersHome.sub("foo/bar/../../baz"); // Resolves to "/home/username/baz"; does not throw an exception
 *   usersHome.sub("foo/../../baz"); // Resolves to "/home/baz"; throws an exception
 *   HOME_ROOT.sub("username").sub("foo/../../bar"); Resolves to "/home/bar"; does not throw an exception because HOME_ROOT is rooted to "/home"
 *
 */
public class RootedPath extends File {

    private final RootedPath root;

    /**
     * Constructs a new RootedPath using the String as the root directory.
     * @param root The root path to use for this instance.
     */
    public RootedPath(String root) {
        super(root);
        this.root = this;
    }

    /**
     * Constructs a new RootedPath using the File as the root directory.
     * @param root The root path to use for this instance.
     */
    public RootedPath(File root) {
        super(root.getPath());
        this.root = this;
    }

    /**
     * Constructs a new RootedPath using the URI as the root directory.
     * @param root The root path to use for this instance.
     */
    public RootedPath(URI root) {
        super(root);
        this.root = this;
    }

    private RootedPath(final RootedPath parent, final String childPath) throws IOException {
        super(parent, childPath);
        this.root = parent.root;
        validateThis();
    }

    /**
     * Constructs a new RootedPath instance representing the subdirectory or file represented by the child
     * parameter. Will throw an exception if the child parameter represents a path outside of the root of this
     * RootedPath.
     *
     * @param child The subdirectory or descendant file the new RootedPath will represent
     * @return A RootedPath representing the subdirectory.
     * @throws IOException If the underlying filesystem throws an exception while canonicalizing the path
     */
    public RootedPath sub(final String child) throws IOException {
        if (child.equals("") || child.equals(".")) {
            return this;
        }
        return new RootedPath(this, child);
    }

    /**
     * @return Returns a RootedPath where the root has been set to reflect the current directory of this
     * RootedPath instance.
     */
    public RootedPath chroot() {
        if (this == this.root) {
            return this;
        }
        return new RootedPath(this);
    }

    private void validateThis() throws IOException {
        File thisPath = this.getCanonicalFile();
        final File rootPath = this.root.getCanonicalFile();

        while (thisPath != null) {
            if (thisPath.equals(rootPath)) {
                return;
            }
            thisPath = thisPath.getParentFile();
        }
        throw new IllegalArgumentException("Child is not contained within the tree's root.");
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RootedPath)) {
            return false;
        }
        RootedPath path = (RootedPath)other;
        if (!super.equals(path)) {
            return false;
        }
        if (this.root == this) {
            return true;
        }
        return this.root.equals(path.root);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (this.root != this) {
            hash = root.hashCode();
        }

        return super.hashCode() ^ hash;
    }

}