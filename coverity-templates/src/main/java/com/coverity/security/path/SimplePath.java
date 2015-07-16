package com.coverity.security.path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a path on the filesystem, extending the java.io.File class with logic to apply runtime enforcement
 * preventing unintended path traversal patterns. The goal is to avoid patterns leading to unintended path manipulation
 * defects in which an attacker may be able to manipulate the target path beyond the developer's intent.
 *
 * This class offers two ways of instantiating a SimplePath. The first is using the template() method, which can be used
 * as follows:
 *
 *   SimplePath path = SimplePath.template("/var/lib/myapp/users/$0/myfiles/$1", username, filename + ".txt");
 *
 * Alternatively, one may create a SimplePath instance using the String constructor:
 *
 *   SimplePath path = new SimplePath("/var/lib/myapp/users");
 *
 * In the latter case, care should be taken to only use constant or trusted strings in constructing the initial object.
 * The below traversal patterns should then be used to access dynamically determined paths.
 *
 * This class offers three forms of traversing paths once the initial object is created. The first reuses the template
 * pattern from the constructor:
 *
 *   SimplePath usersRoot = new SimplePath("/var/lib/myapp/users");
 *   SimplePath file = usersRoot.pathTemplate("$0/myfiles/$1", username, filename + ".txt");
 *
 * You can also use the sub() to traverse individual path components:
 *
 *   SimplePath usersRoot = new SimplePath("/var/lib/myapp/users");
 *   SimplePath file = usersRoot.sub(username).sub("myfiles").sub(filename + ".txt");
 *
 * For convenience, sub() accepts a variable number of arguments, so the above is equivalent to
 *
 *   SimplePath usersRoot = new SimplePath("/var/lib/myapp/users");
 *   SimplePath file = usersRoot.sub(username, "myfiles", filename + ".txt");
 *
 * Finally, you may traverse to an arbitrary sub-path using the path() method:
 *
 *   SimplePath userRoot = new SimplePath("/var/lib/myapp/users").sub(username);
 *   SimplePath file = userRoot.path("myfiles/myreport.txt");
 *
 * All of these patterns enforce runtime checks to prevent unintended path traversals. Read the documentation on each
 * of these methods and constructors for details.
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
        return new SimplePath(resolveTemplate(format, values));
    }

    private static String resolveTemplate(final String format, final String ... values) {
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
        return sb.toString();
    }

    /**
     * Returns a SimplePath instance representing the descendant of this instance represented by the templated path.
     * The template takes the same form as the template() method.
     *
     * @see SimplePath#template
     *
     * @param format The template string used to specify the child path. For safety, this should be a constant string.
     * @param values The substitution parameters to use in the child path.
     * @return A SimplePath representing the descendant path of this instance.
     */
    public SimplePath pathTemplate(final String format, final String ... values) {
        return new SimplePath(this, resolveTemplate(format, values));
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
     * @param pathname An arbitrary path name. No enforcement is placed on the path specified by this string, so only
     *                 constant values or trusted data should be passed to this constructor.
     */
    public SimplePath(String pathname) {
        super(pathname);
    }

    /**
     * Constructs a new SimplePath instance where subPath can contain an arbitrary path. The assumption is that any
     * calling method will have already validated the subPath argument.
     *
     * @param parent The parent path.
     * @param subPath The arbitrary subpath of the parent.
     */
    private SimplePath(SimplePath parent, String subPath) {
        super(parent, subPath);
    }

    /**
     * This method should be used to traverse descendant paths when the developer is able to specify individual
     * directories. For example:
     *
     *   SimplePath blogRoot = dataRoot.sub("blogs", this.getBlogName());
     *   SimplePath blogPost = blogRoot.sub(
     *     new SimpleDateFormat("yyyy").format(this.getPostDate()),
     *     new SimpleDateFormat("MM").format(this.getPostDate()),
     *     new SimpleDateFormat("dd").format(this.getPostDate()),
     *     this.getPostId() + ".txt");
     *
     * @param children Each argument must represent a single path component and therefore cannot contain a path
     *                 separator character. This method also disallows upwards traversals, i.e. ".." elements.
     * @return A new SimplePath instance where the children arguments specify descendant directories or files.
     */
    public SimplePath sub(final String ... children) {
        if (children.length == 0) {
            return this;
        }

        List<String> validChildren = new ArrayList<String>(children.length);
        for (String child : children) {
            if (child == null || child.length() == 0 || child.equals(".")) {
                continue;
            }

            if (isBlackListed(child)) {
                throw new IllegalArgumentException("Invalid child argument: " + child);
            }

            validChildren.add(child);
        }

        if (validChildren.size() == 0) {
            return this;
        }

        return new SimplePath(this, pathJoin(validChildren));
    }

    /**
     * This method should be used to traverse paths when the developer needs to allow an arbitrary number of paths to
     * be traversed and/or needs to allow for upwards traversals within the path. This method only enforces that the
     * resulting path represents the same path or a descendant. An example use case might be:
     *
     *   SimplePath uploadRoot = dataRoot.sub("uploads").sub(username);
     *   SimplePath requestedFile = uploadRoot.path(request.getParameter("filePath");
     *   FileInputStream fis = new FileInputStream(requestedFile);
     *   ...
     *
     * The child parameter can contain an arbitrary path string including path separators and upwards
     * traversals. However, an exception will be thrown in the resolved directory is not a descendant of this
     * SimplePath instance.
     *
     * @param child The subdirectory or descendant file the new SimplePath will represent
     * @return A SimplePath instance representing the subdirectory of this instance.
     */
    public SimplePath path(final String child) {
        if (child.equals("") || child.equals(".")) {
            return this;
        }
        final File subDir = new File(this, child);
        validate(this, subDir);
        return new SimplePath(subDir.getPath());
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

    @Override
    public SimplePath getParentFile() {
        File parent = super.getParentFile();
        return (parent == null ? null : new SimplePath(parent.getPath()));
    }

    private static String pathJoin(final List<String> s) {
        if (s.size() == 0) { return ""; }
        if (s.size() == 1) { return s.get(0); }
        final StringBuilder sb = new StringBuilder(s.get(0));
        for (int i = 1; i < s.size(); i++) {
            sb.append(File.separatorChar).append(s.get(i));
        }
        return sb.toString();
    }

}
