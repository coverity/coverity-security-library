package com.coverity.security.path;

import org.testng.annotations.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import static org.testng.Assert.*;

public class RootedPathTest {
    @Test
    public void testJavadoc() throws IOException {
        RootedPath HOME_ROOT = new RootedPath("/home");
        RootedPath usersHome = HOME_ROOT.sub("username");

        RootedPath testPath = usersHome.sub("foo/bar/../../baz");
        assertEquals(testPath.getCanonicalPath(), "/home/username/baz");

        boolean exceptionCaught = false;
        try {
            usersHome.sub("foo/../../baz");
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
    }

    @Test
    public void testTrivialRoots() throws IOException {
        RootedPath ROOT = new RootedPath("/home");
        assertEquals(ROOT.sub(""), ROOT);
        assertEquals(ROOT.sub("."), ROOT);
    }

    @Test
    public void testUriRootedPath() throws URISyntaxException, IOException {
        RootedPath path = new RootedPath(new URI("file:///foo/bar"));
        path = path.sub("fizz").sub("buzz");
        assertEquals(path.getPath(), "/foo/bar/fizz/buzz");
    }
}
