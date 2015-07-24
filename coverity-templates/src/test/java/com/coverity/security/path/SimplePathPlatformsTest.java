package com.coverity.security.path;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SimplePathPlatformsTest {

    @Test
    public void testKnownPlatform() {
        assertTrue(isWindows() || isUnix(),
                "Testing on an unknown platform. Please update SimplePathPlatformsTest with tests relevant to this "
                + "platform: " + System.getProperty("os.name"));
    }

    @Test
    public void testWindowsSeparators() throws IOException {
        if (!isWindows()) {
            return;
        }

        // Check that the myriad of mixing forward and backslashes are recognized
        assertEquals(new SimplePath("/a/b").path("x/../y/z\\/d/../../e\\f/g").sub("x", "y", "z").getCanonicalPath(), "C:\\a\\b\\y\\e\\f\\g\\x\\y\\z");
        // Check that embedding an upwards traversals between various types of slashes DOES traverses
        assertEquals(new SimplePath("/a/b").path("x/y\\../z/..\\q/y/../z\\..").getCanonicalPath(), "C:\\a\\b\\x\\q");

        // Check that exceptions get thrown when various slashes are put into sub()
        assertExceptionOnSub(new SimplePath("/a/b"), "xy/z");
        assertExceptionOnSub(new SimplePath("/a/b"), "xy\\z");
        // Check that the mixing of slashes to achieve upwards traversal is caught
        assertExceptionOnPath(new SimplePath("/a/b"), "x/y\\../..\\..\\z");
    }

    @Test
    public void testUnixSeparators() throws IOException {
        if (!isUnix()) {
            return;
        }

        // Check that upwards traversals and backslashes in paths don't do anything unexpected.
        assertEquals(new SimplePath("/a/b").path("x/../y/z\\/d/../../e\\f/g").sub("x\\y", "z").getCanonicalPath(), "/a/b/y/e\\f/g/x\\y/z");
        // Check that embedding an upwards traversals between various types of slashes don't actually traverse.
        assertEquals(new SimplePath("/a/b").path("x/y\\../z/..\\q").sub("\\..", "z").getCanonicalPath(), "/a/b/x/y\\../z/..\\q/\\../z");

        // Check that exceptions get thrown when slashes (the only path separator) are put into sub()
        boolean exceptionThrown = false;
        try {
            new SimplePath("/a/b").sub("xy/z");
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }
    private static boolean isUnix() {
        final String osName = System.getProperty("os.name");
        return osName.equals("Linux")
                || osName.endsWith("BSD")
                || osName.startsWith("MacOS");
    }

    private static void assertExceptionOnSub(SimplePath path, String ... sub) {
        boolean exceptionThrown = false;
        try {
            path.sub(sub);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private static void assertExceptionOnPath(SimplePath path, String p) {
        boolean exceptionThrown = false;
        try {
            path.path(p);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}
