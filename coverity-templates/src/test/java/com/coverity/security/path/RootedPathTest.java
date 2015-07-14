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
        usersHome = usersHome.chroot();

        RootedPath testPath = usersHome.sub("foo/bar/../../baz");
        assertEquals(testPath.getCanonicalPath(), "/home/username/baz");

        boolean exceptionCaught = false;
        try {
            usersHome.sub("foo/../../baz");
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        testPath = HOME_ROOT.sub("username").sub("foo/../../bar");
        assertEquals(testPath.getCanonicalPath(), "/home/bar");
    }

    @Test
    public void testTrivialRoots() throws IOException {
        RootedPath ROOT = new RootedPath("/home");
        assertEquals(ROOT.sub(""), ROOT);
        assertEquals(ROOT.sub("."), ROOT);
        assertEquals(ROOT.chroot(), ROOT);
    }

    @Test
    public void testEqualsAndHashCode() throws IOException {
        RootedPath ROOT = new RootedPath("/home");
        RootedPath userHome = ROOT.sub("user");

        assertFalse(ROOT.equals(new File("/home")));
        assertNotEquals(userHome.chroot().hashCode(), userHome.hashCode());
        assertNotEquals(userHome.chroot(), userHome);

        assertEquals(new RootedPath("/home").sub("xyz"), new RootedPath("/home").sub("xyz"));
        assertEquals(new RootedPath("/home").sub("xyz").hashCode(), new RootedPath("/home").sub("xyz").hashCode());
    }

    @Test
    public void testUriRootedPath() throws URISyntaxException, IOException {
        RootedPath path = new RootedPath(new URI("file:///foo/bar"));
        path = path.sub("fizz").sub("buzz");
        assertEquals(path.getPath(), "/foo/bar/fizz/buzz");
    }
}
