package com.coverity.security.path;

import com.google.errorprone.annotations.CompileTimeConstant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Represents a path on the filesystem, extending the <code>java.io.File</code> class with logic to apply runtime enforcement
 * preventing unintended path traversal patterns. The goal is to avoid patterns leading to unintended path manipulation
 * defects in which an attacker may be able to manipulate the target path beyond the developer's intent.</p>
 *
 * <p>This class offers two ways of instantiating a <code>SimplePath</code>. The first is using the
 * <code>template()</code> method, which can be used as follows:</p>
 *
 * <p><code>SimplePath path = SimplePath.template("/var/lib/myapp/users/$0/myfiles/$1", username, filename + ".txt");</code></p>
 *
 * <p>Alternatively, one may create a <code>SimplePath</code> instance using the String constructor:</p>
 *
 * <p><code>SimplePath path = new SimplePath("/var/lib/myapp/users");</code></p>
 *
 * <p>In the latter case, care should be taken to only use constant or trusted strings in constructing the initial object.
 * The below traversal patterns should then be used to access dynamically determined paths.</p>
 *
 * <p>This class offers three forms of traversing paths once the initial object is created. The first reuses the template
 * pattern from the constructor:</p>
 *
 * <p><code>SimplePath usersRoot = new SimplePath("/var/lib/myapp/users");<br/>
 * SimplePath file = usersRoot.pathTemplate("$0/myfiles/$1", username, filename + ".txt");</code></p>
 *
 * <p>You can also use the <code>sub()</code> to traverse individual path components:</p>
 *
 * <p><code>SimplePath usersRoot = new SimplePath("/var/lib/myapp/users");<br/>
 * SimplePath file = usersRoot.sub(username).sub("myfiles").sub(filename + ".txt");</code></p>
 *
 * <p>For convenience, <code>sub()</code> accepts a variable number of arguments, so the above is equivalent to</p>
 *
 * <p><code>SimplePath usersRoot = new SimplePath("/var/lib/myapp/users");<br/>
 * SimplePath file = usersRoot.sub(username, "myfiles", filename + ".txt");</code></p>
 *
 * <p>Finally, you may traverse to an arbitrary sub-path using the <code>path()</code> method:</p>
 *
 * <p><code>SimplePath userRoot = new SimplePath("/var/lib/myapp/users").sub(username);<br/>
 * SimplePath file = userRoot.path("myfiles/myreport.txt");</code></p>
 *
 * <p>All of these patterns enforce runtime checks to prevent unintended path traversals. Read the documentation on each
 * of these methods and constructors for details.</p>
 *
 * <p>Note: Since the JDK doesn't offer any API which describes all of a platform's path separator characters, this
 * class determines all the path separators for the platform using a dynamic check when the class is first loaded. It
 * does this by iterating through all single-width UTF-16 characters, but in particular doesn't check surrogate pairs.
 * If you are running Java on a platform which uses a surrogate-pair of UTF-16 characters as a path separator, this
 * class may not provide safety against path manipulation defects. (That said, at the time of writing there are no known
 * such platforms.)</p>
 *
 */
public class SimplePath extends File {

    /**
     * A cached array of the path separator characters used by this JVM's default FileSystem provider.
     */
    private static final char[] separatorChars = getPathSeparators();
    /**
     * Blacklisted characters that never get to be in any path ever.
     */
    private static final char[] BLACKLISTED_CHARS = new char[] { '\0' };

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
     * Decides if a path component string is blacklisted, which is to say it is the disallowed value <code>".."</code>
     * or it contains a path separator character.
     *
     * @param s The string being evaluated.
     * @return Whether or not the string is blacklisted according to the above.
     */
    private static boolean isBlackListedComponent(String s) {
        if (s.equals("..")) {
            return true;
        }

        final char[] chars = s.toCharArray();
        final char[] sepChars = separatorChars;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            for (int j = 0 ; j < sepChars.length; j++) {
                if (c == sepChars[j]) {
                    return true;
                }
            }
            for (int j = 0; j < BLACKLISTED_CHARS.length; j++) {
                if (c == BLACKLISTED_CHARS[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Decides if a path has any blacklisted characters. This just checks for characters in the BLACKLISTED_CHARS
     * array.
     *
     * @param s The string being evaluated.
     * @return Whether or not the string is blacklisted according to the above.
     */
    private static boolean isBlackListedPath(String s) {
        final char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            for (int j = 0; j < BLACKLISTED_CHARS.length; j++) {
                if (c == BLACKLISTED_CHARS[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>Builds a <code>SimplePath</code> instance using a template string that allows you to specify individual path components.
     * The path will be parsed using the slash <code>'/'</code> character as the separator, regardless of the platform.
     * Placeholders are written as <code>$0</code>, <code>$1</code>, etc. For example</p>
     *
     * <p><code>SimplePath.template("/tmp/$0/xyz/$1/$0", "foo", "bar")</code></p>
     *
     * <p>would produce a path representing <code>"/tmp/foo/xyz/bar/foo"</code>.</p>
     *
     * <p>The <code>$0</code> placeholder must be the entire path component; for example</p>
     *
     * <p><code>SimplePath.template("/tmp/$0/$1.txt", "foo", "bar")</code></p>
     *
     * <p>Would result in the path string <code>"/tmp/foo/$1.txt"</code>. The correct way to get the intended result
     * above would be</p>
     *
     * <p><code>SimplePath.template("/tmp/$0/$1", "foo", "bar" + ".txt");</code></p>
     *
     * @param format The template string.
     * @param values The values to insert into the template string, used one-to-one with the <code>$</code> placeholder
     *               values in the template string.
     * @return The resolved SimplePath instance based on the template and values.
     */
    public static SimplePath template(@CompileTimeConstant final String format, final String ... values) {
        @SuppressWarnings("CompileTimeConstant") // resolveTemplate format argument is @CompileTimeConstant, and performs validation on values
        SimplePath simplePath = new SimplePath(resolveTemplate(format, values));
        return simplePath;
    }

    private static String resolveTemplate(final String format, final String ... values) {
        final String[] pieces = format.split("/", -1);
        final StringBuilder sb = new StringBuilder();
        int placeHolderIndex;
        for (int i = 0; i < pieces.length; i++) {
            final String piece = pieces[i];
            if ((placeHolderIndex = parsePlaceHolder(piece)) != -1) {
                final String value = values[placeHolderIndex];
                if (isBlackListedComponent(value)) {
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
     * Returns a <code>SimplePath</code> instance representing the descendant of this instance represented by the templated path.
     * The template takes the same form as the <code>template()</code> method.
     *
     * @see SimplePath#template
     *
     * @param format The template string used to specify the child path. For safety, this should be a constant string.
     * @param values The substitution parameters to use in the child path.
     * @return A <code>SimplePath</code> representing the descendant path of this instance.
     */
    public SimplePath pathTemplate(@CompileTimeConstant final String format, final String ... values) {
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
     * Constructs a <code>SimplePath</code> instance with the same semantics as <code>java.io.File(String)</code>.
     * No checking is done on this input, so it may be a fully expanded path.
     *
     * @param pathname An arbitrary path name. No enforcement is placed on the path specified by this string, so only
     *                 constant values or trusted data should be passed to this constructor.
     */
    public SimplePath(@CompileTimeConstant final String pathname) {
        super(pathname);
        if (isBlackListedPath(pathname)) {
            throw new IllegalArgumentException("Path contains invalid characters.");
        }
    }

    /**
     * Constructs a new <code>SimplePath</code> instance where <code>subPath</code> can contain an arbitrary path. The
     * assumption is that any calling method will have already validated the <code>subPath</code> argument.
     *
     * @param parent The parent path.
     * @param subPath The arbitrary subpath of the parent.
     */
    private SimplePath(SimplePath parent, String subPath) {
        super(parent, subPath);
    }

    /**
     * <p>This method should be used to traverse descendant paths when the developer is able to specify individual
     * directories. For example:</p>
     *
     * <p><code>SimplePath blogRoot = dataRoot.sub("blogs", this.getBlogName());<br/>
     * SimplePath blogPost = blogRoot.sub(<br/>
     * &nbsp;&nbsp;new SimpleDateFormat("yyyy").format(this.getPostDate()),<br/>
     * &nbsp;&nbsp;new SimpleDateFormat("MM").format(this.getPostDate()),<br/>
     * &nbsp;&nbsp;new SimpleDateFormat("dd").format(this.getPostDate()),<br/>
     * &nbsp;&nbsp;this.getPostId() + ".txt");</code></p>
     *
     * @param children Each argument must represent a single path component and therefore cannot contain a path
     *                 separator character. This method also disallows upwards traversals, i.e. <code>".."</code>
     *                 elements.
     * @return A new <code>SimplePath</code> instance where the children arguments specify descendant directories or files.
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

            if (isBlackListedComponent(child)) {
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
     * <p>This method should be used to traverse paths when the developer needs to allow an arbitrary number of paths to
     * be traversed and/or needs to allow for upwards traversals within the path. This method only enforces that the
     * resulting path represents the same path or a descendant. An example use case might be:</p>
     *
     * <p><code>SimplePath uploadRoot = dataRoot.sub("uploads").sub(username);<br/>
     *   SimplePath requestedFile = uploadRoot.path(request.getParameter("filePath");<br/>
     *   FileInputStream fis = new FileInputStream(requestedFile);<br/>
     *   ...</code></p>
     *
     * <p>The child parameter can contain an arbitrary path string including path separators and upwards
     * traversals. However, an exception will be thrown in the resolved directory is not a descendant of this
     * SimplePath instance.</p>
     *
     * @param child The subdirectory or descendant file the new <code>SimplePath</code> will represent
     * @return A <code>SimplePath</code> instance representing the subdirectory of this instance.
     */
    public SimplePath path(final String child) {
        if (child.equals("") || child.equals(".")) {
            return this;
        }
        if (isBlackListedPath(child)) {
            throw new IllegalArgumentException("Path contains an invalid character.");
        }
        final File subDir = new File(this, child);
        validate(this, subDir);
        @SuppressWarnings("CompileTimeConstant") // Above line does the necessary validation
        SimplePath subPath = new SimplePath(subDir.getPath());
        return subPath;
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
        if (parent == null) {
            return null;
        }
        @SuppressWarnings("CompileTimeConstant") // Upwards traversal is intentional inside this method
        SimplePath parentPath = new SimplePath(parent.getPath());
        return parentPath;
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
