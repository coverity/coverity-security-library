package com.coverity.security.path;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class SimplePathTest {
    @Test
    public void testTraversal() {
        SimplePath path = new SimplePath("/foo/bar");
        path = path.sub("fizz").sub("bu\\zz");
        assertEquals(path.getPath(), "/foo/bar/fizz/bu\\zz");

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
        assertEquals(path.getPath(), "/foo/bar/fizz/buzz/child");

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

        assertEquals(new SimplePath("/foo/bar").sub("fizz/buzz".split("/")).getPath(), "/foo/bar/fizz/buzz");
        assertEquals(path.sub("fizz/buzz".split("/")).getPath(), "/foo/bar/fizz/buzz/child/fizz/buzz");

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
        assertEquals(path.getPath(), "/var/lib/userdata/username/reports/filename.txt");

        path = SimplePath.template("/var/lib/userdata/$0/reports/$1.txt", "username", "filename");
        assertEquals(path.getPath(), "/var/lib/userdata/username/reports/$1.txt");

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
        assertEquals(path.getPath(), "/a/z/x/y/z");
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
        assertEquals(path.getParent(), "foo/bar");
        assertEquals(path.getParentFile().getPath(), "foo/bar");
    }

    @Test
    public void testJavadoc() throws IOException {
        SimplePath HOME_ROOT = new SimplePath("/home");
        SimplePath usersHome = HOME_ROOT.sub("username");

        SimplePath testPath = usersHome.path("foo/bar/../../baz");
        assertEquals(testPath.getCanonicalPath(), "/home/username/baz");

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
        assertEquals(reportDir.getPath(), "/home/username/userdata/reports/2015");

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
    public void testCanonicalPathException() throws IOException {
        /* The only reliable way I've found to induce an IOException when calling getCanonicalPath() is to include
           a null character in the path.
         */

        SimplePath BAD_ROOT = new SimplePath("/home/foo\u0000bar");
        assertEquals(BAD_ROOT.sub("xyz").getPath(), "/home/foo\u0000bar/xyz");

        boolean exceptionCaught = false;
        try {
            BAD_ROOT.path("xyz");
        } catch (RuntimeException e) {
            exceptionCaught = true;
            assertEquals(e.getCause().getClass(), IOException.class);
        }
        assertTrue(exceptionCaught);

        SimplePath ROOT = new SimplePath("/home/foobar");
        assertEquals(ROOT.sub("xy\u0000z").getPath(), "/home/foobar/xy\u0000z");

        exceptionCaught = false;
        try {
            ROOT.path("xy\u0000z");
        } catch (RuntimeException e) {
            exceptionCaught = true;
            assertEquals(e.getCause().getClass(), IOException.class);
        }
        assertTrue(exceptionCaught);

    }
}
