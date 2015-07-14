package com.coverity.security.path;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a path on the filesystem, extending the java.io.File class with logic to apply runtime enforcement
 * preventing path traversal patterns. The goal is to avoid patterns leading to unintended path manipulation defects in
 * which an attacker may be able to manipulate the target path beyond the developer's intent.
 *
 * There are two primary ways one may use this class: either using the template() method or using the sub(child)
 * builder pattern. The former can be used as follows:
 *
 *   SimplePath path = SimplePath.template("/var/lib/myapp/users/$0/myfiles/$1", username, filename + ".txt");
 *
 * Using the sub() method provides an API for traversing one directory at a time:
 *
 *   SimplePath path = new SimplePath("/var/lib/myapp").sub("users").sub(username).sub("myfiles").sub(filename + ".txt");
 *
 * Both patterns enforce that the individual path components don't contain any path separators so that calling
 * sub() or using the "$0" placeholder strings in the template only result in a single directory traversal. The class
 * also always rejects ".." as a path component. Use getParentFile() instead to achieve upwards traversals when desired.
 *
 */
public class SimplePath extends File {

    /**
     * A cached array of the path separator characters used by this JVM's default FileSystem provider.
     */
    private static final char[] separatorChars = getPathSeparators();

    /**
     * Loops through all possible characters, returning an array of those which the default FileSystem treats as
     * path separators.
     *
     * @return An array of all path separator characters, as interpreted by this JVM's default FileSystem provider.
     */
    private static char[] getPathSeparators() {
        List<Character> chars = new ArrayList<Character>();
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            final File file = new File("a" + File.separator + "b" + ((char)i) + "c");
            if (file.getParentFile().getName().equals("b")) {
                chars.add(Character.valueOf((char)i));
            }
        }

        final char[] result = new char[chars.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = chars.get(i).charValue();
        }
        return result;
    }

    /**
     * Decides if a path component string is blacklisted, which is to say it is the disallowed value ".." or it contains
     * a path separator character.
     *
     * @param s The string being evaluated.
     * @return Whether or not the string is blacklist according to the above.
     */
    private static boolean isBlackListed(String s) {
        if (s.equals("..")) {
            return true;
        }

        final char[] chars = s.toCharArray();
        final char[] sepChars = separatorChars;
        for (int i = 0; i < chars.length; i++) {
            for (int j = 0 ; j < sepChars.length; j++) {
                if (chars[i] == sepChars[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Builds a SimplePath instance using a template string that allows you to specify individual path components.
     * The path will be parsed using the slash '/' character as the separator, regardless of the platform. Placeholders
     * are written as $0, $1, etc. For example
     *
     *   SimplePath.template("/tmp/$0/xyz/%1/$0", "foo", "bar")
     *
     * would produce a path representing "/tmp/foo/xyz/bar/foo".
     *
     * The $0 placeholder must be the entire path component; for example
     *
     *   SimplePath.template("/tmp/$0/$1.txt", "foo", "bar")
     *
     * Would result in the path string "/tmp/foo/$1.txt". The correct way to get the intended result above would be
     *
     *   SimplePath.template("/tmp/$0/$1", "foo", "bar" + ".txt");
     *
     * @param format The template string.
     * @param values The values to insert into the template string, used one-to-one with the %s placeholder values in
     *               the template string.
     * @return The resolved SimplePath instance based on the template and values.
     */
    public static SimplePath template(final String format, final String ... values) {
        final String[] pieces = format.split("/", -1);
        final StringBuilder sb = new StringBuilder();
        int placeHolderIndex;
        for (int i = 0; i < pieces.length; i++) {
            final String piece = pieces[i];
            if ((placeHolderIndex = parsePlaceHolder(piece)) != -1) {
                final String value = values[placeHolderIndex];
                if (isBlackListed(value)) {
                    throw new IllegalArgumentException("Child path cannot have path separators or be an upwards traversal");
                }
                sb.append(value);
            } else {
                sb.append(piece);
            }
            if (i != pieces.length-1) {
                sb.append(File.separatorChar);
            }
        }
        return new SimplePath(sb.toString());
    }

    private static int parsePlaceHolder(String s) {
        if (s.length() == 0) {
            return -1;
        }

        final char[] chars = s.toCharArray();
        if (chars[0] != '$') {
            return -1;
        }
        int value = 0;
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] < '0' || chars[i] > '9') {
                return -1;
            }
            value = (value * 10) + (chars[i] - '0');
        }
        return value;
    }

    /**
     * Constructs a SimplePath instance with the same semantics as java.io.File(String). No checking is done on this
     * input, so it may be a fully expanded path.
     *
     * @param pathname
     */
    public SimplePath(String pathname) {
        super(pathname);
    }

    /**
     * Constructs a SimplePath instance representing the same path as the provided File instance. No checking on done
     * on the input, so it may represent a fully expanded path.
     *
     * @param file
     */
    public SimplePath(File file) {
        super(file.getPath());
    }


    public SimplePath(String parent, String child) {
        super(parent, child);
        if (isBlackListed(child)) {
            throw new IllegalArgumentException("Child path cannot have path separators or be an upwards traversal");
        }
    }

    public SimplePath(File parent, String child) {
        super(parent, child);
        if (isBlackListed(child)) {
            throw new IllegalArgumentException("Child path cannot have path separators or be an upwards traversal");
        }
    }

    public SimplePath(String parent, String[] children) {
        super(parent, pathJoin(children));
        for (final String child : children) {
            if (isBlackListed(child)) {
                throw new IllegalArgumentException("Child path cannot have path separators or be an upwards traversal");
            }
        }
    }

    public SimplePath(File parent, String[] children) {
        super(parent, pathJoin(children));
        for (final String child : children) {
            if (isBlackListed(child)) {
                throw new IllegalArgumentException("Child path cannot have path separators or be an upwards traversal");
            }
        }
    }

    public SimplePath(URI uri) {
        super(uri);
    }

    public SimplePath sub(final String child) {
        if (child.equals("") || child.equals(".")) {
            return this;
        }
        return new SimplePath(this, child);
    }

    @Override
    public SimplePath getParentFile() {
        File parent = super.getParentFile();
        return (parent == null ? null : new SimplePath(parent));
    }

    private static String pathJoin(final String[] s) {
        if (s.length == 0) { return ""; }
        if (s.length == 1) { return s[0]; }
        final StringBuilder sb = new StringBuilder(s[0]);
        for (int i = 1; i < s.length; i++) {
            sb.append(File.separatorChar).append(s[i]);
        }
        return sb.toString();
    }

}
