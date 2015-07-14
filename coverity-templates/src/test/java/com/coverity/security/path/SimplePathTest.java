package com.coverity.security.path;

import org.testng.annotations.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

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
        SimplePath path = new SimplePath("/foo/bar/fizz/buzz", "child");
        assertEquals(path.getPath(), "/foo/bar/fizz/buzz/child");

        boolean exception = false;
        try {
            new SimplePath("/foo/bar/fizz/buzz", "child/grandchild");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            new SimplePath("/foo/bar/fizz/buzz", "..");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            new SimplePath(path, "child/grandchild");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            new SimplePath(path, "..");
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        assertEquals(new SimplePath("/foo/bar", "fizz/buzz".split("/")).getPath(), "/foo/bar/fizz/buzz");
        assertEquals(new SimplePath(path, "fizz/buzz".split("/")).getPath(), "/foo/bar/fizz/buzz/child/fizz/buzz");

        exception = false;
        try {
            new SimplePath("/foo/bar", "fizz/../buzz".split("/"));
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            new SimplePath(path, "fizz/../buzz".split("/"));
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
    public void testUriSimplePath() throws URISyntaxException {
        SimplePath path = new SimplePath(new URI("file:///foo/bar"));
        path = path.sub("fizz").sub("buzz");
        assertEquals(path.getPath(), "/foo/bar/fizz/buzz");
    }

    @Test
    public void testFileConstructor() throws URISyntaxException {
        SimplePath path = new SimplePath(new File("foo/bar"));
        path = path.sub("fizz").sub("buzz");
        assertEquals(path.getPath(), "foo/bar/fizz/buzz");
    }
}
