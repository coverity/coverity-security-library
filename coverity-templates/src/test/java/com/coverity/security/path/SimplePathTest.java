package com.coverity.security.path;

import com.google.errorprone.annotations.CompileTimeConstant;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

public class SimplePathTest {
    @Test
    public void testTraversal() {
        SimplePath path = new SimplePath("/foo/bar");
        path = path.sub("fizz").sub("buzz");
        assertEquals(path.getPath(), localizePath("/foo/bar/fizz/buzz"));

        assertSame(path, path.sub("."));

        boolean exception = false;
        try {
            path.sub("..");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            path.sub("foo/bar");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            path.sub("foo/..");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testConstructors() {
        SimplePath path = new SimplePath("/foo/bar/fizz/buzz").sub("child");
        assertEquals(path.getPath(), localizePath("/foo/bar/fizz/buzz/child"));

        boolean exception = false;
        try {
            new SimplePath("/foo/bar/fizz/buzz").sub("child/grandchild");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            new SimplePath("/foo/bar/fizz/buzz").sub("..");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            path.sub("child/grandchild");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            path.sub("..");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        assertEquals(new SimplePath("/foo/bar").sub("fizz/buzz".split("/")).getPath(), localizePath("/foo/bar/fizz/buzz"));
        assertEquals(path.sub("fizz/buzz".split("/")).getPath(), localizePath("/foo/bar/fizz/buzz/child/fizz/buzz"));

        exception = false;
        try {
            new SimplePath("/foo/bar").sub("fizz/../buzz".split("/"));
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            path.sub("fizz/../buzz".split("/"));
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testTemplate() {
        SimplePath path = SimplePath.template("/var/lib/userdata/$0/reports/$1", "username", "filename.txt");
        assertEquals(path.getPath(), localizePath("/var/lib/userdata/username/reports/filename.txt"));

        path = SimplePath.template("/var/lib/userdata/$0/reports/$1.txt", "username", "filename");
        assertEquals(path.getPath(), localizePath("/var/lib/userdata/username/reports/$1.txt"));

        boolean exception = false;
        try {
            SimplePath.template("/var/lib/userdata/$0/reports/$1", "..", "filename.txt");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            SimplePath.template("/var/lib/userdata/$0/reports/$1", "username/foo", "filename.txt");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            SimplePath.template("/var/lib/userdata/$0/reports/$1", "username", "bar/filename.txt");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @Test
    public void testTemplateRepeatParam() {
        SimplePath path = SimplePath.template("/a/$2/$0/$1/$2", "x", "y", "z");
        assertEquals(path.getPath(), localizePath("/a/z/x/y/z"));
    }

    @Test
    public void testTemplateUnsetParam() {
        boolean exception = false;
        try {
            SimplePath.template("/a/$1", "x");
        } catch (Exception e) {
            exception = true;
        }
    }

    @Test
    public void testGetParent() {
        SimplePath path = new SimplePath("foo/bar/baz");
        assertEquals(path.getParent(), localizePath("foo/bar"));
        assertEquals(path.getParentFile().getPath(), localizePath("foo/bar"));
    }

    @Test
    public void testJavadoc() throws IOException {
        SimplePath HOME_ROOT = new SimplePath("/home");
        SimplePath usersHome = HOME_ROOT.sub("username");

        SimplePath testPath = usersHome.path("foo/bar/../../baz");
        assertEquals(testPath.getCanonicalPath(), localizeCanonPath("/home/username/baz"));

        boolean exceptionCaught = false;
        try {
            usersHome.path("foo/../../baz");
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
    }

    @Test
    public void testTrivialRoots() throws IOException {
        SimplePath ROOT = new SimplePath("/home");
        assertEquals(ROOT.sub(), ROOT);
        assertEquals(ROOT.sub(""), ROOT);
        assertEquals(ROOT.sub("."), ROOT);

        assertEquals(ROOT.path(""), ROOT);
        assertEquals(ROOT.path("."), ROOT);
    }

    @Test
    public void testPathTemplate() {
        SimplePath ROOT = new SimplePath("/home");
        SimplePath reportDir = ROOT.pathTemplate("$0/userdata/reports/$1/", "username", "2015");
        assertEquals(reportDir.getPath(), localizePath("/home/username/userdata/reports/2015"));

        boolean exceptionCaught = false;
        try {
            ROOT.pathTemplate("$0/userdata/$1/reports", "foo", "..");
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        exceptionCaught = false;
        try {
            ROOT.pathTemplate("$0/userdata/$1/reports", "foo", "a/b");
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
    }

    @Test
    public void testNullBytePaths() {
        SimplePath root = new SimplePath("/foo/bar");
        boolean exceptionCaught = false;
        try {
            root.path("a/b/evil\u0000");
        } catch (Exception e) {
            exceptionCaught = true;
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        assertTrue(exceptionCaught);

        exceptionCaught = false;
        try {
            new SimplePath("a/b/evil\u0000");
        } catch (Exception e) {
            exceptionCaught = true;
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        assertTrue(exceptionCaught);

        exceptionCaught = false;
        try {
            root.sub("ab\u0000c");
        } catch (Exception e) {
            exceptionCaught = true;
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        assertTrue(exceptionCaught);
    }

    @Test
    public void testCanonicalPathException() throws IOException {
        SimplePath BAD_ROOT = new ErrorThrowingSimplePath("/foo/bar");

        boolean exceptionCaught = false;
        try {
            BAD_ROOT.path("xyz");
        } catch (RuntimeException e) {
            exceptionCaught = true;
            assertEquals(e.getClass(), RuntimeException.class);
            assertEquals(e.getCause().getClass(), IOException.class);
        }
        assertTrue(exceptionCaught);
    }

    private static class ErrorThrowingSimplePath extends SimplePath {
        public ErrorThrowingSimplePath(@CompileTimeConstant final String pathname) {
            super(pathname);
        }

        @Override
        public File getCanonicalFile() throws IOException {
            throw new IOException();
        }
    }

    private static String localizePath(String s) {
        return new File(s).getPath();
    }
    private static String localizeCanonPath(String s) throws IOException {
        return new File(s).getCanonicalPath();
    }
}
